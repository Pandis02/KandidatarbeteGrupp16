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

/**
 * Service for retrieving and updating offline event logs.
 * Combines information from multiple tables including check-ins, tracked devices, locations,
 * notifications, and recipient info to return enriched event records.
 */
@Service
public class LogService {

    private static final Logger logger = LoggerFactory.getLogger(LogService.class);
    private final JdbcTemplate jdbcTemplate;

    public LogService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Maps a result row from the database into a {@link LogDTO}.
     * Includes handling for nullable `confirmed_false_positive` field.
     */
    private final RowMapper<LogDTO> logRowMapper = (rs, rowNum) -> {
        Boolean confirmed = rs.getObject("confirmed_false_positive") != null
            ? rs.getBoolean("confirmed_false_positive")
            : null;

        return new LogDTO(
            rs.getLong("eventId"),
            rs.getString("device_name"),
            rs.getString("mac_address"),
            rs.getString("offline_since"),
            rs.getString("restored_at"),
            rs.getBoolean("alert_sent"),
            rs.getString("notification_timestamp"),
            rs.getString("notification_recipients"),
            rs.getString("location"),
            rs.getString("tag"),
            confirmed
        );
    };

    /**
     * Retrieves all offline events that occurred within the given date range.
     * Joins with related tables to return detailed notification and device information.
     *
     * @param startDate the start date for filtering (inclusive), or null to skip
     * @param endDate   the end date for filtering (exclusive), or null to skip
     * @return a list of {@link LogDTO} enriched with device and notification context
     */
    public List<LogDTO> findOfflineEvents(LocalDate startDate, LocalDate endDate) {
        StringBuilder query = new StringBuilder("""
            SELECT
                oe.event_id AS eventId,
                oe.mac_address,
                oe.location,
                oe.offline_since,
                oe.restored_at,
                oe.tag,
                oe.confirmed_false_positive,
                COALESCE(td.custom_name, c.hostname) AS device_name,
                nc.timestamp AS notification_timestamp,
                STRING_AGG(nr.recipient_value, '; ') AS notification_recipients,
                CASE
                    WHEN nc.event_id IS NOT NULL THEN TRUE
                    ELSE FALSE
                END AS alert_sent
            FROM
                OfflineEvents oe
            LEFT JOIN
                TrackedDevices td ON oe.mac_address = td.mac_address
            LEFT JOIN
                Locations l ON td.location_id = l.location_id
            LEFT JOIN
                Checkins c ON td.mac_address = c.mac_address
            LEFT JOIN
                Notifications nc ON nc.event_id = oe.event_id
            LEFT JOIN
                NotificationRecipients nr ON nr.notification_id = nc.notification_id
        """);

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

        query.append("""
            GROUP BY 
                oe.event_id,
                oe.mac_address,
                oe.location,
                oe.offline_since,
                oe.restored_at,
                oe.tag,
                oe.confirmed_false_positive,
                td.custom_name,
                c.hostname,
                nc.timestamp,
                nc.event_id
            ORDER BY oe.offline_since DESC
        """);

        return jdbcTemplate.query(query.toString(), params.toArray(), logRowMapper);
    }

    /**
     * Updates the tag associated with a given offline event.
     *
     * @param eventId the ID of the event to update
     * @param tag     the new tag value (e.g., "Power Outage", "Network Issue")
     */
    public void updateTag(Long eventId, String tag) {
        String sql = "UPDATE OfflineEvents SET tag = ? WHERE event_id = ?";
        jdbcTemplate.update(sql, tag, eventId);
    }

    /**
     * Updates the confirmed false positive status of a given offline event.
     *
     * @param eventId           the ID of the event to update
     * @param isFalsePositive   true if confirmed as a false positive, false otherwise
     */
    public void updateConfirmedFalsePositive(Long eventId, Boolean isFalsePositive) {
        String sql = "UPDATE OfflineEvents SET confirmed_false_positive = ? WHERE event_id = ?";
        jdbcTemplate.update(sql, isFalsePositive, eventId);
    }
}
