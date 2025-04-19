package kg16.demo.model.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import kg16.demo.model.dto.LogDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class LogService {
    private static final Logger logger = LoggerFactory.getLogger(LogService.class);
    private final JdbcTemplate jdbcTemplate;

    public LogService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<LogDTO> logRowMapper = (rs, rowNum) -> new LogDTO(
        rs.getLong("eventId"),
            rs.getString("device_name"),
            rs.getString("mac_address"),
            rs.getString("offline_since"),
            rs.getString("restored_at"),
            rs.getBoolean("alert_sent"),
            rs.getString("notification_timestamp"),
            rs.getString("notification_recipients"),
            rs.getString("location"),
            rs.getString("tag"));

    public List<LogDTO> findOfflineEvents(LocalDate startDate, LocalDate endDate) {
        StringBuilder query = new StringBuilder("""
                SELECT
                    oe.event_id AS eventId,
                    oe.mac_address,
                    oe.location,
                    oe.offline_since,
                    oe.restored_at,
                    oe.tag,
                    COALESCE(td.custom_name, c.hostname) AS device_name,
                    nc.timestamp AS notification_timestamp,
                    STRING_AGG(nr.recipient_value, '; ') AS notification_recipients,
                    CASE
                        WHEN nc.event_id IS NOT NULL THEN TRUE
                        ELSE FALSE
                    END AS alert_sent
                FROM
                    OfflineEvents oe
                JOIN
                    TrackedDevices td ON oe.mac_address = td.mac_address
                LEFT JOIN
                    Locations l ON td.location_id = l.location_id
                LEFT JOIN
                    Checkins c ON td.mac_address = c.mac_address
                LEFT JOIN
                    Notifications nc ON nc.event_id = oe.event_id
                LEFT JOIN
                    NotificationRecipients nr ON nr.notification_id = nc.notification_id
                GROUP BY oe.event_id
                """);

        // Apply filtering based on startDate and endDate
        List<Object> params = new ArrayList<>();
        if (startDate != null || endDate != null) {
            query.append("WHERE ");
            if (startDate != null) {
                query.append("oe.offline_since >= ? ");
                params.add(startDate.atStartOfDay());
            }
            if (endDate != null) {
                if (startDate != null) {
                    query.append("AND ");
                }
                query.append("oe.offline_since < ? ");
                params.add(endDate.plusDays(1).atStartOfDay());
            }
        }

        // Add ORDER BY clause
        query.append("ORDER BY oe.offline_since DESC");

        // Execute the query with parameters
        return jdbcTemplate.query(query.toString(), params.toArray(), logRowMapper);
    }

    public void updateTag(Long eventId, String tag) {
        String sql = "UPDATE OfflineEvents SET tag = ? WHERE event_id = ?";
        jdbcTemplate.update(sql, tag, eventId);
    }
    
    

}