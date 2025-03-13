package kg16.demo.model.services;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import kg16.demo.model.dto.AdminSettings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TrackerService {
    private final ScanService sc;
    private final CheckinService cs;
    private final AdminSettingsService assc;
    private final JdbcTemplate jdbc;

    private static final Logger logger = LoggerFactory.getLogger(TrackerService.class);

    public TrackerService(ScanService sc, CheckinService cs, AdminSettingsService assc, JdbcTemplate jdbc) {
        this.sc = sc;
        this.cs = cs;
        this.assc = assc;
        this.jdbc = jdbc;
    }

    /**
    * runs every 1 second, with an initial delay of 16 seconds so devices don't immediately get flagged offline
    */
    @Scheduled(fixedDelay = 1000, initialDelay = 16000)
    public void track() {
        /* 
            1. Get tracked devices that are currently not flagged offline but should be according to our threshold
            2. Flag as offline in the OfflineEvents table
        */

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
    }

    /** 
     * Get all unflagged mac_addresses meaning we have not already put them into OfflineEvents
     * AND they have not been seen in a certain time frame
     */
    private List<SummarizedDevice> getAllUnflaggedDevicesNotSeenIn(int seconds) {
        String sql = """
                    SELECT td.mac_address, td.custom_name, s.hostname, s.ip_address, s.last_seen, c.last_checkin
                    FROM TrackedDevices td
                    LEFT JOIN Scans s ON td.mac_address = s.mac_address
                    LEFT JOIN Checkins c ON td.mac_address = c.mac_address
                    WHERE (
                        GREATEST (COALESCE(s.last_seen, '1970-01-01'), COALESCE(c.last_checkin, '1970-01-01')) < NOW() - INTERVAL '%d' SECOND
                    )
                    AND td.enabled = TRUE  -- Only consider enabled devices
                    AND NOT EXISTS (
                        SELECT 1 FROM OfflineEvents oe
                        WHERE oe.mac_address = td.mac_address
                        AND oe.restored_at IS NULL
                    );
                """;

        return jdbc.query(String.format(sql, seconds), (r, rowNum) -> {
            return new SummarizedDevice(
                    r.getString("mac_address"),
                    r.getString("custom_name"),
                    r.getString("hostname"),
                    r.getString("ip_address"),
                    r.getTimestamp("last_seen"),
                    r.getTimestamp("last_checkin"));
        });
    }

    /**
     * Flag a certain device as offline
     */
    private void flag(SummarizedDevice device) {
        String sql = """
                    INSERT INTO OfflineEvents (mac_address, offline_since)
                    VALUES (?, ?)
                """;

        try {
            jdbc.update(sql, device.macAddress, device.lastActive());
            logger.info("Device " + device.macAddress + " flagged as offline.");
        } catch (DataAccessException e) {
            logger.error("Failed to update OfflineEvents for mac address: " + device.macAddress, e);
        }
    }

    private record SummarizedDevice(
            String macAddress,
            String customName,
            String hostname,
            String ipAddress,
            Timestamp lastSeen,
            Timestamp lastCheckin) {

        Timestamp lastActive() {
            if (lastSeen == null)
                return lastCheckin;
            if (lastCheckin == null)
                throw new RuntimeException("Both last_seen & last_checkin cannot be NULL, this is impossible");
            return lastSeen.toLocalDateTime().isAfter(lastCheckin.toLocalDateTime()) ? lastSeen : lastCheckin;
        }

        @Override
        public String toString() {
            return "Device(" + macAddress + ")";
        }
    }
}
