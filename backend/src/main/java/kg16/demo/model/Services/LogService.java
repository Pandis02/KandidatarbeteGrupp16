package kg16.demo.model.services;

import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import kg16.demo.model.dto.LogDTO;

@Service
public class LogService {

    private final JdbcTemplate jdbcTemplate;

    public LogService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper to map ResultSet to LogDTO
    private final RowMapper<LogDTO> logRowMapper = (rs, rowNum) -> new LogDTO(
        rs.getString("hostname"),
        rs.getString("ip_address"),
        rs.getString("mac_address"),
        rs.getString("last_seen"),
        rs.getString("offline_since"),
        rs.getString("restored_at")
    );

    // Query logs with optional filters
    public List<LogDTO> findLogs(String startDate, String endDate, Integer alertType) {
        StringBuilder query = new StringBuilder(
            "SELECT Scans.hostname, Scans.ip_address, Scans.mac_address, " +
            "Scans.last_seen, OfflineEvents.offline_since, OfflineEvents.restored_at " +
            "FROM OfflineEvents " +
            "JOIN Scans ON OfflineEvents.mac_address = Scans.mac_address WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        if (startDate != null) {
            query.append(" AND OfflineEvents.offline_since >= ?");
            params.add(startDate);
        }
        if (endDate != null) {
            query.append(" AND OfflineEvents.offline_since <= ?");
            params.add(endDate);
        }
        if (alertType != null) {
            query.append(" AND Scans.status = ?");
            params.add(alertType);
        }

        // Ensure query returns a List of LogDTO objects
        return jdbcTemplate.query(query.toString(), params.toArray(), logRowMapper);
    }
}
