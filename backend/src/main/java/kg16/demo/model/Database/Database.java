package kg16.demo.model.Database;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import kg16.demo.dto.ScanDTO;
import kg16.demo.model.records.Checkin;
import kg16.demo.model.records.ScanRecord;

@Service
public class Database {
    private final JdbcTemplate jdbc;

    public Database(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    // single query based insert/update
    public void insert_checkin(String mac_address) {
        String sql = """
                    MERGE INTO Checkins c
                    USING (SELECT '%s' AS mac_address, NOW() AS last_checkin) AS new_data
                    ON c.mac_address = new_data.mac_address
                    WHEN MATCHED THEN
                        UPDATE SET c.last_checkin = new_data.last_checkin
                    WHEN NOT MATCHED THEN
                        INSERT (mac_address, last_checkin) VALUES (new_data.mac_address, new_data.last_checkin);
                """;
        jdbc.execute(String.format(sql, mac_address));
    }

    // get all checkins
    public List<Checkin> get_all_checkins() {
        String sql = "SELECT * FROM Checkins;";

        return jdbc.query(sql, (r, rowNum) -> {
            return new Checkin(r.getString("mac_address"), r.getTimestamp("last_checkin"));
        });
    }

    // get all colums of mac_addresses that have not checked in, in the last x minutes
    public List<Checkin> get_all_checkins_older_than(int minutes) {
        LocalDateTime before = LocalDateTime.now().minusMinutes(minutes);
        String sql = "SELECT * FROM Checkins WHERE last_checkin < ?";

        return jdbc.query(sql, new Object[] { Timestamp.valueOf(before) },
                (rs, rowNum) -> new Checkin(rs.getString("mac_address"), rs.getTimestamp("last_checkin")));
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