package kg16.demo.model.services;

import kg16.demo.model.records.Checkin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CheckinService {
    private final JdbcTemplate jdbc;
    private static final Logger logger = LoggerFactory.getLogger(CheckinService.class);

    public CheckinService(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    // single query based insert/update
    @Transactional
    public void upsertCheckin(String macAddress, String hostname) {
        Timestamp t = Timestamp.valueOf(LocalDateTime.now());

        // MERGE operation for Checkins table
        String mergeSQL = """
                MERGE INTO Checkins c
                USING (VALUES (?, ?, ?)) AS new_data(mac_address, hostname, last_checkin)
                ON c.mac_address = new_data.mac_address
                WHEN MATCHED THEN
                    UPDATE SET c.last_checkin = new_data.last_checkin, c.hostname = new_data.hostname
                WHEN NOT MATCHED THEN
                    INSERT (mac_address, hostname, last_checkin) VALUES (new_data.mac_address, new_data.hostname, new_data.last_checkin)
                """;

        // Update OfflineEvents for device if previously marked offline
        String updateSQL = """
                UPDATE OfflineEvents
                SET restored_at = ?
                WHERE mac_address = ?
                AND restored_at IS NULL
                """;

        try {
            jdbc.update(mergeSQL, macAddress, hostname, t);
            jdbc.update(updateSQL, t, macAddress);
        } catch (DataAccessException e) {
            logger.error("Failed to upsert checkin for mac address: " + macAddress, e);
        }
    }

    // get all checkins
    public List<Checkin> getAllCheckins() {
        String sql = "SELECT * FROM Checkins;";

        return jdbc.query(sql, (r, rowNum) -> {
            return new Checkin(
                    r.getString("mac_address"),
                    r.getString("hostname"),
                    r.getTimestamp("last_checkin"));
        });
    }

    // get all devices that have not checked in, in the last x minutes
    public List<Checkin> getAllCheckinsOlderThan(int minutes) {
        LocalDateTime before = LocalDateTime.now().minusMinutes(minutes);
        String sql = "SELECT * FROM Checkins WHERE last_checkin < ?";

        return jdbc.query(sql, new Object[] { Timestamp.valueOf(before) },
                (rs, rowNum) -> new Checkin(rs.getString("mac_address"),
                        rs.getString("hostname"),
                        rs.getTimestamp("last_checkin")));
    }

    // get all checkins
    public List<Checkin> getAllUnregisteredCheckins() {
        String sql = """
                SELECT
                    c.mac_address,
                    c.hostname,
                    c.last_checkin
                FROM Checkins c
                WHERE NOT EXISTS (SELECT 1 FROM TrackedDevices td WHERE td.mac_address = c.mac_address);
                """;
        ;
        return jdbc.query(sql, (rs, rowNum) -> new Checkin(
                rs.getString("mac_address"),
                rs.getString("hostname"),
                rs.getTimestamp("last_checkin")));
    }

    // Forget a device that DOES NOT exist in TrackedDevices.
    public void forget(String macAddress) {
        String sql = "DELETE FROM Checkins WHERE mac_address = ? AND NOT EXISTS (SELECT 1 FROM TrackedDevices WHERE mac_address = ?);";
        jdbc.update(sql, macAddress, macAddress);
    }

    // register a device that is not registered yet
    public void register(String macAddress, String customName, int locationId) {
        if (customName != null && !customName.equals("")) {
            String sql = "INSERT INTO TrackedDevices VALUES (?, ?, TRUE, ?);";
            jdbc.update(sql, macAddress, customName, locationId);
        } else {
            String sql = "INSERT INTO TrackedDevices (mac_address, enabled, location_id) VALUES (?, TRUE, ?);";
            jdbc.update(sql, macAddress, locationId);
        }

    }
}
