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

/**
 * Service responsible for managing device check-ins and related state transitions.
 * Handles insertions, updates, and resolution of offline events based on latest check-in timestamps.
 */
@Service
public class CheckinService {

    private final JdbcTemplate jdbc;
    private static final Logger logger = LoggerFactory.getLogger(CheckinService.class);

    public CheckinService(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    /**
     * Inserts or updates the latest check-in for a device.
     * Also updates any open offline event by setting its {@code restored_at} timestamp.
     *
     * @param macAddress the device's MAC address
     * @param hostname   the device's hostname (may be updated)
     */
    @Transactional
    public void upsertCheckin(String macAddress, String hostname) {
        Timestamp t = Timestamp.valueOf(LocalDateTime.now());

        // Insert or update latest check-in
        String mergeSQL = """
            MERGE INTO Checkins c
            USING (VALUES (?, ?, ?)) AS new_data(mac_address, hostname, last_checkin)
            ON c.mac_address = new_data.mac_address
            WHEN MATCHED THEN
                UPDATE SET c.last_checkin = new_data.last_checkin, c.hostname = new_data.hostname
            WHEN NOT MATCHED THEN
                INSERT (mac_address, hostname, last_checkin)
                VALUES (new_data.mac_address, new_data.hostname, new_data.last_checkin)
        """;

        // Resolve any offline event still marked as "unrestored"
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

    /**
     * Retrieves all check-ins in the system.
     *
     * @return list of {@link Checkin} records
     */
    public List<Checkin> getAllCheckins() {
        String sql = "SELECT * FROM Checkins;";
        return jdbc.query(sql, (r, rowNum) -> new Checkin(
            r.getString("mac_address"),
            r.getString("hostname"),
            r.getTimestamp("last_checkin")
        ));
    }

    /**
     * Retrieves all devices that have not checked in within the past {@code minutes}.
     *
     * @param minutes the age threshold in minutes
     * @return list of devices with outdated check-ins
     */
    public List<Checkin> getAllCheckinsOlderThan(int minutes) {
        LocalDateTime before = LocalDateTime.now().minusMinutes(minutes);
        String sql = "SELECT * FROM Checkins WHERE last_checkin < ?";
        return jdbc.query(sql, new Object[]{ Timestamp.valueOf(before) }, 
            (rs, rowNum) -> new Checkin(
                rs.getString("mac_address"),
                rs.getString("hostname"),
                rs.getTimestamp("last_checkin")
            )
        );
    }

    /**
     * Returns all devices in the check-in table that are not registered in {@code TrackedDevices}.
     * Useful for onboarding or identifying unmanaged devices.
     *
     * @return list of unregistered device check-ins
     */
    public List<Checkin> getAllUnregisteredCheckins() {
        String sql = """
            SELECT c.mac_address, c.hostname, c.last_checkin
            FROM Checkins c
            WHERE NOT EXISTS (
                SELECT 1 FROM TrackedDevices td
                WHERE td.mac_address = c.mac_address
            );
        """;
        return jdbc.query(sql, (rs, rowNum) -> new Checkin(
            rs.getString("mac_address"),
            rs.getString("hostname"),
            rs.getTimestamp("last_checkin")
        ));
    }

    /**
     * Forgets a device from the check-in table only if it is not listed in {@code TrackedDevices}.
     *
     * @param macAddress the MAC address of the device to forget
     */
    public void forget(String macAddress) {
        String sql = """
            DELETE FROM Checkins
            WHERE mac_address = ?
            AND NOT EXISTS (
                SELECT 1 FROM TrackedDevices
                WHERE mac_address = ?
            )
        """;
        jdbc.update(sql, macAddress, macAddress);
    }

    /**
     * Registers a device in {@code TrackedDevices} with optional custom name.
     * If {@code customName} is null or empty, it falls back to using only the MAC address and location ID.
     *
     * @param macAddress  the device's MAC address
     * @param customName  an optional custom name/label for the device
     * @param locationId  the location ID to associate the device with
     */
    public void register(String macAddress, String customName, int locationId) {
        if (customName != null && !customName.isBlank()) {
            String sql = "INSERT INTO TrackedDevices VALUES (?, ?, TRUE, ?);";
            jdbc.update(sql, macAddress, customName, locationId);
        } else {
            String sql = "INSERT INTO TrackedDevices (mac_address, enabled, location_id) VALUES (?, TRUE, ?);";
            jdbc.update(sql, macAddress, locationId);
        }
    }
}
