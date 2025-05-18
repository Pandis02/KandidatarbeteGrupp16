package kg16.demo.model.services;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import kg16.demo.model.dto.MostNotifiedDevice;
import kg16.demo.model.dto.NotificationChannelCount;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service responsible for collecting statistics related to notification activity.
 * This includes identifying the most notified devices and distribution of notification channels.
 */
@Service
public class NotificationStatsService {

    private final JdbcTemplate jdbc;

    /**
     * Constructs the service using the provided JDBC template.
     *
     * @param jdbc the JDBC template for executing queries
     */
    public NotificationStatsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Retrieves the top devices that triggered the most notifications during the specified time range.
     * This is useful for identifying devices that are generating frequent alerts.
     *
     * @param from the start of the time range
     * @param to   the end of the time range
     * @return a list of {@link MostNotifiedDevice} sorted by notification count (descending)
     */
    public List<MostNotifiedDevice> findMostNotifiedDevices(LocalDateTime from, LocalDateTime to) {
        String sql = """
            SELECT e.mac_address, COUNT(*) AS notification_count
            FROM Notifications n
            JOIN OfflineEvents e ON n.event_id = e.event_id
            WHERE e.offline_since BETWEEN ? AND ?
            GROUP BY e.mac_address
            ORDER BY notification_count DESC
        """;

        return jdbc.query(sql, (rs, rowNum) ->
            new MostNotifiedDevice(
                rs.getString("mac_address"),
                rs.getInt("notification_count")
            ), from, to
        );
    }

    /**
     * Returns a breakdown of the number of notifications sent by channel type (e.g., Email, SMS, Slack)
     * for a specified time range.
     *
     * @param from the start of the time range
     * @param to   the end of the time range
     * @return a list of {@link NotificationChannelCount}, one per channel type
     */
    public List<NotificationChannelCount> getNotificationChannelDistributionBetween(LocalDateTime from, LocalDateTime to) {
        String sql = """
            SELECT r.recipient_type, COUNT(*) AS count
            FROM Notifications n
            JOIN OfflineEvents e ON n.event_id = e.event_id
            JOIN NotificationRecipients nr ON nr.notification_id = n.notification_id
            JOIN Recipients r ON r.recipient_value = nr.recipient_value
            WHERE e.offline_since BETWEEN ? AND ?
            GROUP BY r.recipient_type
        """;

        return jdbc.query(sql, (rs, rowNum) ->
            new NotificationChannelCount(
                rs.getString("recipient_type"),
                rs.getInt("count")
            ), from, to
        );
    }
}
