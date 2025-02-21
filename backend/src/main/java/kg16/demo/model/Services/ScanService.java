package kg16.demo.model.Services;

import kg16.demo.dto.ScanDTO;
import kg16.demo.model.records.ScanRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;


@Service
public class ScanService {
    private final JdbcTemplate jdbc;

    public ScanService(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    // Upsert scan (Insert if not exists, otherwise update)
    public void upsertScan(String hostname, String ipAddress, String macAddress, Integer status) {
        if (!isValidMacAddress(macAddress) || !isValidIpAddress(ipAddress) || hostname.isBlank()) {
            throw new IllegalArgumentException("Invalid input data for scan.");
        }

        String sql = """
            MERGE INTO Scans AS s
            USING (SELECT 1) AS dummy
            ON s.mac_address = ?
            WHEN MATCHED THEN
                UPDATE SET s.hostname = ?, 
                        s.ip_address = ?, 
                        s.status = ?, 
                        s.last_seen = CURRENT_TIMESTAMP
            WHEN NOT MATCHED THEN
                INSERT (mac_address, hostname, ip_address, status, last_seen) 
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP);
        """;

        jdbc.update(sql, macAddress, hostname, ipAddress, status, macAddress, hostname, ipAddress, status);
    }


    private boolean isValidMacAddress(String macAddress) {
        return macAddress != null && (macAddress.equalsIgnoreCase("Unknown MAC") || 
                macAddress.matches("([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}"));
    }


    private boolean isValidIpAddress(String ipAddress) {
        return ipAddress != null && (ipAddress.equalsIgnoreCase("Unknown") ||
                ipAddress.matches("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b") ||  // IPv4
                ipAddress.matches("([a-fA-F0-9:]+:+)+[a-fA-F0-9]+"));    // IPv6
    }

    // Retrieve all scan results
    public List<ScanRecord> getAllScans() {
        String sql = "SELECT mac_address, ip_address, hostname, status, last_seen FROM Scans";
        return jdbc.query(sql, (rs, rowNum) -> 
            new ScanRecord(
                rs.getString("mac_address"),
                rs.getString("ip_address"),
                rs.getString("hostname"),
                rs.getInt("status"),
                rs.getTimestamp("last_seen")
            )
        );
    }

    // Retrieve scans older than X minutes
    public List<ScanRecord> getOldScans(int minutes) {
        LocalDateTime before = LocalDateTime.now().minusMinutes(minutes);
        String sql = "SELECT mac_address, ip_address, hostname, status, last_seen FROM Scans WHERE last_seen < ?";
        
        return jdbc.query(sql, new Object[] { Timestamp.valueOf(before) },
                (rs, rowNum) -> new ScanRecord(
                        rs.getString("mac_address"),
                        rs.getString("ip_address"),
                        rs.getString("hostname"),
                        rs.getInt("status"),
                        rs.getTimestamp("last_seen")
                ));
    }

    public List<ScanRecord> getScans(Integer status, String sortBy, String order, int page, int size) {
        String sql = "SELECT * FROM Scans";
    
        // Add filtering if status is provided
        if (status != null) {
            sql += " WHERE status = ?";
        }
    
        // Add sorting
        sql += " ORDER BY " + sortBy + " " + (order.equalsIgnoreCase("desc") ? "DESC" : "ASC");
    
        // Apply pagination (LIMIT + OFFSET)
        sql += " LIMIT ? OFFSET ?";
    
        Object[] params;
        if (status != null) {
            params = new Object[]{status, size, page * size};
        } else {
            params = new Object[]{size, page * size};
        }
    
        return jdbc.query(sql, params, (rs, rowNum) -> new ScanRecord(
            rs.getString("hostname"),
            rs.getString("ip_address"),
            rs.getString("mac_address"),
            rs.getInt("status"),
            rs.getTimestamp("last_seen")
        ));
    }
    
    
    public int countScans(Integer status) {
        String sql = "SELECT COUNT(*) FROM Scans";
        return jdbc.queryForObject(sql, status != null ? new Object[]{status} : new Object[]{}, Integer.class);
    }

    public List<ScanDTO> getPaginatedScans(int page, int size) {
        String sql = "SELECT * FROM Scans ORDER BY last_seen DESC LIMIT ? OFFSET ?";
        int offset = page * size;
        
        return jdbc.query(sql, new Object[] { size, offset }, (rs, rowNum) -> 
            new ScanDTO(
                rs.getString("hostname"),
                rs.getString("ip_address"),
                rs.getString("mac_address"),
                rs.getInt("status"),
                rs.getTimestamp("last_seen")
            )
        );
    }
}
