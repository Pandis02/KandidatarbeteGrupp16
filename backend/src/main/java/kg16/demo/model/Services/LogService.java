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
            rs.getString("ip_address"),
            rs.getString("mac_address"),
            rs.getString("offline_since"),
            rs.getString("restored_at")
    );

    public List<LogDTO> findOfflineEvents(LocalDate startDate, LocalDate endDate) {
        StringBuilder query = new StringBuilder(
                "SELECT Scans.hostname, Scans.ip_address, Scans.mac_address, " +
                "OfflineEvents.offline_since, OfflineEvents.restored_at " +
                "FROM OfflineEvents " +
                "JOIN Scans ON OfflineEvents.mac_address = Scans.mac_address WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        if (startDate != null) {
            query.append(" AND OfflineEvents.offline_since >= ?");
            params.add(startDate.atStartOfDay()); 
        }
        if (endDate != null) {
            query.append(" AND OfflineEvents.offline_since < ?"); 
            params.add(endDate.plusDays(1).atStartOfDay()); 
        }

        query.append(" ORDER BY OfflineEvents.offline_since DESC");

        try {
            return jdbcTemplate.query(query.toString(), params.toArray(), logRowMapper);
        } catch (DataAccessException e) {
            logger.error("Failed to fetch offline events logs", e);
            return new ArrayList<>(); // Return an empty list in case of an error
        }
    }
}