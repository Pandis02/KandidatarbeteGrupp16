package kg16.demo.model.services;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import kg16.demo.model.dto.*;

import java.time.*;
import java.util.*;

@Service
public class NotificationStatsService {
    
    private final JdbcTemplate jdbc;

    public NotificationStatsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

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
