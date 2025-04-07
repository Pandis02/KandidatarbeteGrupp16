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
            rs.getString("device_name"),
            rs.getString("mac_address"),
            rs.getString("offline_since"),
            rs.getString("restored_at"),
            rs.getBoolean("alert_sent"),
            rs.getString("notification_message"),
            rs.getString("notification_timestamp"),
            rs.getString("notification_recipient"),
            rs.getString("notification_type"),
            rs.getString("building"),
            rs.getString("room"));

    public List<LogDTO> findOfflineEvents(LocalDate startDate, LocalDate endDate) {
        StringBuilder query = new StringBuilder("""
                SELECT
                    oe.event_id,
                    oe.mac_address,
                    oe.offline_since,
                    oe.restored_at,
                    COALESCE(td.custom_name, c.hostname) AS device_name,
                    l.building,
                    l.room,
                    nc.message AS notification_message,
                    nc.timestamp AS notification_timestamp,
                    rp.recipient_value AS notification_recipient,
                    rp.recipient_type AS notification_type,
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
                LEFT JOIN
                    Recipients rp ON rp.recipient_id = nr.recipient_id
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

}