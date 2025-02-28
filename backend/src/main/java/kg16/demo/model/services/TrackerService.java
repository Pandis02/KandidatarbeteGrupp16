package kg16.demo.model.services;

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
        System.out.println("Fixed Delay Task: " + System.currentTimeMillis());
        /* 
            1. Get tracked devices that are currently not flagged offline
            2. Flag as offline in the OfflineEvents table
            3. Check if devices in OfflineEvents are now back online
            4. 
        */

        // Step 1: Get tracked devices that are currently not flagged offline
        List<String> unflaggedDevices = getAllUnflaggedDevicesNotSeenIn(5);
        System.out.println("Unflagged devices: " + unflaggedDevices);
        
         // Step 2: Flag as offline in the OfflineEvents table
         for (String macAddress : unflaggedDevices) {
            String sql = """
                INSERT INTO OfflineEvents (mac_address, offline_since)
                VALUES (?, NOW())
            """;
            jdbc.update(sql, macAddress);
            System.out.println("Device " + macAddress + " flagged as offline.");
        }
        

        // Step 3: Check if devices in OfflineEvents are now back online
        List<String> offlineDevices = getAllDevicesMarkedOfflineForMoreThan(5);
        System.out.println("Offline devices to check: " + offlineDevices);

        for (String macAddress : offlineDevices) {
            boolean isOnline = checkIfDeviceIsOnline(macAddress);
            if (isOnline) {
                // Step 3: If the device is online, mark it as restored in OfflineEvents
                String sql = """
                    UPDATE OfflineEvents
                    SET restored_at = NOW()
                    WHERE mac_address = ?
                      AND restored_at IS NULL
                """;
                jdbc.update(sql, macAddress);
                System.out.println("Device " + macAddress + " restored.");
            }
            
        }
    }

    /* 
     * Get all unflagged mac_addresses meaning we have not already put them into OfflineEvents
     * AND they have not been seen in a certain time frame
     */
    private List<String> getAllUnflaggedDevicesNotSeenIn(int minutes) {
        String sql = """
                    SELECT td.mac_address
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
        return jdbc.queryForList(String.format(sql, minutes), String.class);
    }

    // Method to get devices that have been marked offline for more than `x` minutes
    private List<String> getAllDevicesMarkedOfflineForMoreThan(int minutes) {
        String sql = String.format("""
            SELECT mac_address
            FROM OfflineEvents
            WHERE restored_at IS NULL
            AND offline_since < NOW() - INTERVAL '%d' MINUTE
        """, minutes);
        
        return jdbc.queryForList(sql, String.class);
    }




    // Method to check if a device is online. It checks if the device has checked in within the last minute.
    // Adjust the interval (e.g., 1 minute) as needed to define when the device is considered online.

    private boolean checkIfDeviceIsOnline(String macAddress) {
        String sql = """
            SELECT COUNT(*)
            FROM Checkins
            WHERE mac_address = ?
            AND last_checkin > NOW() - INTERVAL '1' MINUTE
        """;
        
        Integer count = jdbc.queryForObject(sql, new Object[]{macAddress}, Integer.class);
        return count != null && count > 0;
    }





}
