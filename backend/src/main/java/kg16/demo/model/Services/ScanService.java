package kg16.demo.model.services;

import kg16.demo.model.records.ScanRecord;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ScanService {
    private final JdbcTemplate jdbc;
    private static final Logger logger = LoggerFactory.getLogger(ScanService.class);

    public ScanService(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    // Upsert scan (insert if not exists, otherwise update)
    @Transactional
    public void upsertScan(String hostname, String ipAddress, String macAddress) {
        if (!isValidMacAddress(macAddress) || !isValidIpAddress(ipAddress) || hostname.isBlank()) {
            throw new IllegalArgumentException("Invalid input data for scan.");
        }

        Timestamp t = Timestamp.valueOf(LocalDateTime.now());
        String sql = """
                    MERGE INTO Scans AS s
                    USING (SELECT 1) AS dummy
                    ON s.mac_address = ?
                    WHEN MATCHED THEN
                        UPDATE SET
                                s.hostname = ?,
                                s.ip_address = ?,
                                s.last_seen = ?
                    WHEN NOT MATCHED THEN
                        INSERT (mac_address, hostname, ip_address, last_seen)
                        VALUES (?, ?, ?, ?);
                """;

        // Update OfflineEvents for device if previously marked offline
        String updateSQL = """
                UPDATE OfflineEvents
                SET restored_at = ?
                WHERE mac_address = ?
                AND restored_at IS NULL
                """;

        try {
            jdbc.update(sql, macAddress, hostname, ipAddress, t, macAddress, hostname, ipAddress, t);
            jdbc.update(updateSQL, t, macAddress);
        } catch (DataAccessException e) {
            logger.error("Failed to upsert scan for mac address: " + macAddress, e);
        }
    }

    private boolean isValidMacAddress(String macAddress) {
        return macAddress != null && (macAddress.equalsIgnoreCase("Unknown MAC") ||
                macAddress.matches("([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}"));
    }

    private boolean isValidIpAddress(String ipAddress) {
        return ipAddress != null && (ipAddress.equalsIgnoreCase("Unknown") ||
                ipAddress.matches("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b") || // IPv4
                ipAddress.matches("([a-fA-F0-9:]+:+)+[a-fA-F0-9]+")); // IPv6
    }

    // Retrieve all scan results
    public List<ScanRecord> getAllScans() {
        String sql = "SELECT * FROM Scans";
        return jdbc.query(sql, (rs, rowNum) -> new ScanRecord(
                rs.getString("mac_address"),
                rs.getString("ip_address"),
                rs.getString("hostname"),
                rs.getTimestamp("last_seen")));
    }

    // Retrieve scans older than X minutes
    public List<ScanRecord> getOldScans(int minutes) {
        LocalDateTime before = LocalDateTime.now().minusMinutes(minutes);
        String sql = "SELECT * FROM Scans WHERE last_seen < ?";

        return jdbc.query(sql, new Object[] { Timestamp.valueOf(before) },
                (rs, rowNum) -> new ScanRecord(
                        rs.getString("mac_address"),
                        rs.getString("ip_address"),
                        rs.getString("hostname"),
                        rs.getTimestamp("last_seen")));
    }

    public List<ScanRecord> getScans(String sortBy, String order, int page, int size) {
        // Define allowed sorting fields to prevent SQL injection
        List<String> allowedSortFields = List.of("mac_address", "ip_address", "hostname", "last_seen");

        if (!allowedSortFields.contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sorting column: " + sortBy);
        }

        // Ensure safe sorting direction
        String sortDirection = order.equalsIgnoreCase("desc") ? "DESC" : "ASC";

        // SQL Query with sorting and pagination
        String sql = "SELECT * FROM Scans";

        sql += " ORDER BY " + sortBy + " " + sortDirection + " LIMIT ? OFFSET ?";

        Object[] params;
        params = new Object[] { size, page * size };

        return jdbc.query(sql, params, (rs, rowNum) -> new ScanRecord(
                rs.getString("mac_address"),
                rs.getString("ip_address"),
                rs.getString("hostname"),
                rs.getTimestamp("last_seen")));
    }

    public int countScans(Integer status) {
        String sql = "SELECT COUNT(*) FROM Scans";
        return jdbc.queryForObject(sql, status != null ? new Object[] { status } : new Object[] {}, Integer.class);
    }
}
