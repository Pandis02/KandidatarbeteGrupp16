package kg16.demo.model.services;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TrackerService {
    private final ScanService sc;
    private final CheckinService cs;
    private final JdbcTemplate jdbc;

    public TrackerService(ScanService sc, CheckinService cs, JdbcTemplate jdbc) {
        this.sc = sc;
        this.cs = cs;
        this.jdbc = jdbc;
    }

    // runs every 10 seconds
    @Scheduled(fixedDelay = 10000)
    public void track() {
        /* 
            1. Get tracked devices that are currently not flagged offline but should be according to our threshold
            2. Flag as offline in the OfflineEvents table
        */

        // Step 1: Get tracked devices that are currently not flagged offline
        List<SummarizedDevice> unflaggedDevices = getAllUnflaggedDevicesNotSeenIn(6);
        System.out.println("Unflagged devices: " + unflaggedDevices);

        // Step 2: Flag as offline in the OfflineEvents table
        for (SummarizedDevice device : unflaggedDevices) {
            String sql = """
                        INSERT INTO OfflineEvents (mac_address, offline_since)
                        VALUES (?, ?)
                    """;

            jdbc.update(sql, device.macAddress, device.lastActive());
            System.out.println("Device " + device.macAddress + " flagged as offline.");
        }
    }

    /* 
     * Get all unflagged mac_addresses meaning we have not already put them into OfflineEvents
     * AND they have not been seen in a certain time frame
     */
    private List<SummarizedDevice> getAllUnflaggedDevicesNotSeenIn(int minutes) {
        String sql = """
                    SELECT td.mac_address, td.custom_name, s.hostname, s.ip_address, s.last_seen, c.last_checkin
                    FROM TrackedDevices td
                    LEFT JOIN Scans s ON td.mac_address = s.mac_address
                    LEFT JOIN Checkins c ON td.mac_address = c.mac_address
                    WHERE (
                        GREATEST (COALESCE(s.last_seen, '1970-01-01'), COALESCE(c.last_checkin, '1970-01-01')) < NOW() - INTERVAL '%d' MINUTE
                    )
                    AND NOT EXISTS (
                        SELECT 1 FROM OfflineEvents oe
                        WHERE oe.mac_address = td.mac_address
                        AND oe.restored_at IS NULL
                    );
                """;

        return jdbc.query(String.format(sql, minutes), (r, rowNum) -> {
            return new SummarizedDevice(
                    r.getString("mac_address"),
                    r.getString("custom_name"),
                    r.getString("hostname"),
                    r.getString("ip_address"),
                    r.getTimestamp("last_seen"),
                    r.getTimestamp("last_checkin"));
        });
    }

    private record SummarizedDevice(
            String macAddress,
            String customName,
            String hostname,
            String ipAddress,
            Timestamp lastSeen,
            Timestamp lastCheckin) {

        Timestamp lastActive() {
            if (lastSeen == null)
                return lastCheckin;
            if (lastCheckin == null)
                throw new RuntimeException("Both last_seen & last_checkin cannot be NULL, this is impossible");
            return lastSeen.toLocalDateTime().isAfter(lastCheckin.toLocalDateTime()) ? lastSeen : lastCheckin;
        }

        @Override
        public String toString() {
            return "Device(" + macAddress + ")";
        }
    }
}
