package kg16.demo.model.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
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
            rs.getString("hostname"),
            rs.getString("mac_address"),
            rs.getString("offline_since"),
            rs.getString("restored_at"),
            rs.getBoolean("alert_sent"),
            rs.getString("notification_message"),
            rs.getString("notification_timestamp"),
            rs.getString("notification_recipient"),
            rs.getString("notification_type"),
            rs.getString("building"),
            rs.getString("room")
    );

    public List<LogDTO> findOfflineEvents(LocalDate startDate, LocalDate endDate) { 
        StringBuilder query = new StringBuilder(
            "SELECT " +
            "    oe.event_id, " +
            "    oe.mac_address, " +
            "    oe.offline_since, " +
            "    oe.restored_at, " +
            "    td.custom_name AS hostname, " +
            "    l.building, " + 
            "    l.room, " + 
            "    ne.message AS notification_message, " +
            "    ne.timestamp AS notification_timestamp, " +
            "    nr.recipient AS notification_recipient, " +
            "    nr.type AS notification_type, " +
            "    CASE " +
            "        WHEN ne.event_id IS NOT NULL THEN TRUE " +
            "        ELSE FALSE " +
            "    END AS alert_sent " +
            "FROM " +
            "    OfflineEvents oe " +
            "JOIN " +
            "    TrackedDevices td ON oe.mac_address = td.mac_address " +
            "LEFT JOIN " +
            "    Locations l ON td.location_id = l.location_id " + 
            "LEFT JOIN " +
            "    NotificationEvents ne ON oe.mac_address = ne.mac_address " +
            "    AND oe.event_id = ne.event_id " +
            "LEFT JOIN " +
            "    NotificationRecipients nr ON ne.event_id = nr.event_id " +
            "ORDER BY " +
            "    oe.offline_since DESC"
        );

        List<Object> params = new ArrayList<>();

        if (startDate != null) {
            query.append(" AND oe.offline_since >= ?");
            params.add(startDate.atStartOfDay()); 
        }
        if (endDate != null) {
            query.append(" AND oe.offline_since < ?"); 
            params.add(endDate.plusDays(1).atStartOfDay()); 
        }

        try {
            return jdbcTemplate.query(query.toString(), params.toArray(), logRowMapper);
        } catch (DataAccessException e) {
            logger.error("Failed to fetch offline events logs", e);
            return new ArrayList<>(); // Return an empty list in case of an error
        }
    }
}