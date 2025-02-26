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
            2. Log them into offline events
            3. 
            4. 
        */

        // works and prints out [04-7C-16-DE-4E-08] when I stop sending checkins from my pc
        System.out.println(getAllUnflaggedDevicesNotSeenIn(6));
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
}
