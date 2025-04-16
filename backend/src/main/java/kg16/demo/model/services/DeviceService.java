package kg16.demo.model.services;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DeviceService {
    private final JdbcTemplate jdbc;

    public DeviceService(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    // get all devices in tracked table
    public List<Instance> getAllRegisteredDevices() {
        String sql = """
                    SELECT COALESCE(td.custom_name, c.hostname) AS name, c.last_checkin, td.mac_address, loc.building, loc.room, loc.location_id,
                    CASE
                        WHEN EXISTS (
                            SELECT 1
                            FROM OfflineEvents oe
                            WHERE oe.mac_address = td.mac_address
                            AND oe.restored_at IS NULL
                            AND oe.offline_since = c.last_checkin
                        )
                        THEN FALSE
                        ELSE TRUE
                    END AS online, td.enabled
                    FROM TrackedDevices td
                    JOIN Checkins c ON td.mac_address = c.mac_address
                    LEFT JOIN Locations loc ON td.location_id = loc.location_id;
                """;

        return jdbc.query(sql, (r, rowNum) -> {
            String status = "Paused";
            if (r.getBoolean("enabled")) {
                status = r.getBoolean("online") ? "Online" : "Offline";
            }
            return new Instance(
                    r.getString("name"),
                    r.getTimestamp("last_checkin"),
                    r.getString("mac_address"),
                    r.getString("building"),
                    r.getString("room"),
                    r.getInt("location_id"),
                    status,
                    r.getBoolean("enabled"));
        });
    }

    // set enabled for a particular device
    public void setEnabled(String macAddress, boolean enabled) {
        String sql = "UPDATE TrackedDevices SET enabled = ? WHERE mac_address = ?;";
        jdbc.update(sql, enabled, macAddress);
    }

    // delete a particular device
    public void delete(String macAddress) {
        String sql = "DELETE FROM TrackedDevices WHERE mac_address = ?;";
        jdbc.update(sql, macAddress);
    }

    public record Instance(
            String name,
            Timestamp lastCheckin,
            String macAddress,
            String building,
            String room,
            int locationId,
            String status,
            boolean enabled) {

        @Override
        public String toString() {
            return "Device(" + macAddress + ")";
        }
    }

}
