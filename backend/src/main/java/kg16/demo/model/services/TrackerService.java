package kg16.demo.model.services;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import kg16.demo.model.dto.AdminSettings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TrackerService {
    private final RecipientService rs;
    private final AdminSettingsService assc;
    private final EmailService es;
    private final JdbcTemplate jdbc;
    private boolean noRecipientsErrorWarned = false;

    private static final Logger logger = LoggerFactory.getLogger(TrackerService.class);

    public TrackerService(RecipientService rs, AdminSettingsService assc, EmailService es, JdbcTemplate jdbc) {
        this.rs = rs;
        this.assc = assc;
        this.es = es;
        this.jdbc = jdbc;
    }

    /**
    * runs every 1 second, with an initial delay of 16 seconds so devices don't immediately get flagged offline
    */
    @Scheduled(fixedDelay = 1000, initialDelay = 16000)
    public void track() {
        // Prep
        AdminSettings as = assc.getSettings();
        int intervalSafeEpsilonSeconds = 3;

        // Step 1: Get tracked devices that are currently not flagged offline
        List<SummarizedDevice> unflaggedDevices = getAllUnflaggedDevicesNotSeenIn(
                as.getCheckinIntervalSeconds() + intervalSafeEpsilonSeconds);

        // Step 2: Flag as offline in the OfflineEvents table
        for (SummarizedDevice device : unflaggedDevices) {
            flag(device);
        }

        // Step 3: Get all offline devices that we should send notifications for
        List<OfflineEvent> events = getAllUnNotifiedOfflineEvents(as.getAlertThresholdMinutes());

        // Step 4: Send notifications for each event
        for (OfflineEvent event : events) {
            notify(event);
        }
    }

    /** 
     * Get all unflagged mac_addresses meaning we have not already put them into OfflineEvents
     * AND they have not been seen in a certain time frame
     */
    private List<SummarizedDevice> getAllUnflaggedDevicesNotSeenIn(int seconds) {
        String sql = """
                    SELECT td.mac_address, COALESCE(td.custom_name, c.hostname) as name, c.last_checkin, l.building, l.room
                    FROM TrackedDevices td
                    LEFT JOIN Locations l ON l.location_id = td.location_id
                    JOIN Checkins c ON td.mac_address = c.mac_address
                    WHERE td.enabled = TRUE AND (
                        c.last_checkin < NOW() - INTERVAL '%d' SECOND
                    ) AND NOT EXISTS (
                        SELECT 1 FROM OfflineEvents oe
                        WHERE oe.mac_address = td.mac_address
                        AND oe.restored_at IS NULL
                    );
                """;

        return jdbc.query(String.format(sql, seconds), (r, rowNum) -> {
            return new SummarizedDevice(
                    r.getString("mac_address"),
                    r.getString("name"),
                    r.getTimestamp("last_checkin"),
                    r.getString("building"),
                    r.getString("room"));
        });
    }

    /** 
     * Get all devices in OfflineEvents that have been offline for x minutes and no notification has yet been sent
     */
    private List<OfflineEvent> getAllUnNotifiedOfflineEvents(int minutes) {
        String sql = """
                SELECT oe.event_id, oe.mac_address, COALESCE(td.custom_name, c.hostname) as device_name, oe.offline_since, l.building, l.room
                FROM OfflineEvents oe
                JOIN TrackedDevices td ON td.mac_address = oe.mac_address
                LEFT JOIN Locations l ON l.location_id = td.location_id
                LEFT JOIN Checkins c ON c.mac_address = td.mac_address
                LEFT JOIN Notifications n ON n.event_id = oe.event_id
                WHERE td.enabled AND oe.restored_at IS NULL AND n.notification_id IS NULL AND (
                    oe.offline_since < NOW() - INTERVAL '%d' MINUTE
                );
                """;

        return jdbc.query(String.format(sql, minutes), (r, rowNum) -> {
            return new OfflineEvent(
                    r.getLong("event_id"),
                    r.getString("mac_address"),
                    r.getString("device_name"),
                    r.getTimestamp("offline_since"),
                    r.getString("building"),
                    r.getString("room"));
        });
    }

    /**
     * Flag a certain device as offline
     */
    private void flag(SummarizedDevice device) {
        String sql = """
                    INSERT INTO OfflineEvents (mac_address, location, offline_since)
                    VALUES (?, ?, ?)
                """;

        try {
            jdbc.update(sql, device.macAddress(), device.building() + " " + device.room(), device.lastCheckin());
            logger.info("Device " + device.macAddress() + " flagged as offline.");
        } catch (DataAccessException e) {
            logger.error("Failed to update OfflineEvents for mac address: " + device.macAddress(), e);
        }
    }

    /**
     * Send notification for a certain event
     */
    @Transactional
    private void notify(OfflineEvent event) {
        var recipients = rs.getAllRecipients();

        // Warn only once if no recipients are configured
        if ((recipients == null || recipients.isEmpty())) {
            if (!noRecipientsErrorWarned) {
                noRecipientsErrorWarned = true;
                logger.error("There are no recipients configured for emails.");
            }
            return;
        }

        try {
            // Create SimpleJdbcInsert for notifications
            SimpleJdbcInsert notificationInsert = new SimpleJdbcInsert(jdbc)
                    .withTableName("Notifications")
                    .usingGeneratedKeyColumns("notification_id");

            // Create parameters map
            Map<String, Object> params = new HashMap<>();
            params.put("event_id", event.id);
            params.put("timestamp", Timestamp.valueOf(LocalDateTime.now()));

            // Execute insert and get generated key
            Long notificationId = notificationInsert.executeAndReturnKey(params).longValue();

            logger.info("Notification sent for Device(" + event.macAddress() + ")");

            // Insert recipients
            for (var recipient : recipients) {
                if (!recipient.getRecipientType().equals("email"))
                    continue;

                try {
                    sendMail(recipient.getRecipientValue(), event);
                    jdbc.update("INSERT INTO NotificationRecipients (notification_id, recipient_value) VALUES (?, ?)",
                            notificationId, recipient.getRecipientValue());
                } catch (Exception e) {
                    logger.error("Failed to notify " + recipient.getRecipientValue() + " for " + event.macAddress(), e);
                }
            }
        } catch (DataAccessException e) {
            logger.error("Failed to notify for mac address: " + event.macAddress(), e);
        }
    }

    void sendMail(String recieverEmail, OfflineEvent event) {
        try {
            es.sendAlarmEmail(recieverEmail, event.deviceName() + "(" + event.macAddress() + ")", event.room(),
                    event.building());
        } catch (MessagingException e) {
            logger.error("Failed to send email", e);
        }
    }

    private record SummarizedDevice(
            String macAddress,
            String name,
            Timestamp lastCheckin,
            String building,
            String room) {

        @Override
        public String toString() {
            return "Device(" + macAddress + ")";
        }
    }

    private record OfflineEvent(
            Long id,
            String macAddress,
            String deviceName,
            Timestamp offlineSince,
            String building,
            String room) {
    }
}
