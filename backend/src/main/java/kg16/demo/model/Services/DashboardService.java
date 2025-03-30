package kg16.demo.model.services;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final JdbcTemplate jdbc;

    public DashboardService(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    private List<TopNRecentAlerts> getDashboardDeviceInfo() {
        // Repeat CASE in ORDER BY because H2 does not allow referencing an alias (like status) in the ORDER BY clause
        String sql = """
                    SELECT c.mac_address, c.last_checkin,
                    CASE
                        WHEN oe.restored_at IS NOT NULL THEN 'online'
                        WHEN TIMESTAMPDIFF(MINUTE, c.last_checkin, NOW()) < a.alert_threshold_minutes THEN 'paused'
                        ELSE 'offline'
                    END AS status
                    FROM Checkins c
                    LEFT JOIN OfflineEvents oe ON c.mac_address = oe.mac_address
                    CROSS JOIN (SELECT alert_threshold_minutes FROM AdminSettings ORDER BY id DESC LIMIT 1) a
                    ORDER BY
                        CASE
                            WHEN oe.restored_at IS NOT NULL THEN 3
                            WHEN TIMESTAMPDIFF(MINUTE, c.last_checkin, NOW()) < a.alert_threshold_minutes THEN 2
                            ELSE 1
                        END,
                    c.last_checkin DESC;
                """;

        return jdbc.query(sql, (r, rowNum) -> {
            return new TopNRecentAlerts(
                    r.getString("mac_address"),
                    r.getTimestamp("last_checkin"),
                    r.getString("status"));
        });
    }

    public DeviceStatusCount getDeviceStatusCount() {

        var devices = getDashboardDeviceInfo();

        int onlineCount = 0;
        int tempOfflineCount = 0;
        int offlineCount = 0;

        for (TopNRecentAlerts device : devices) {
            switch (device.status) {
                case "online" -> onlineCount++;
                case "paused" -> tempOfflineCount++;
                default -> offlineCount++;
            }
        }
        int totalComputers = onlineCount + tempOfflineCount + offlineCount;

        return new DeviceStatusCount(onlineCount, tempOfflineCount, offlineCount, totalComputers);
    }

    public List<TopNRecentAlerts> getTopRecentOfflineDevices(int n) {
        var devices = getDashboardDeviceInfo();
        return devices.stream()
                .filter(d -> "offline"
                        .equals(d.status()))
                .limit(n)
                .collect(Collectors.toList());
    }

    public record TopNRecentAlerts(
            String macAddress,
            Timestamp lastCheckin,
            String status) {
    }

    public record DeviceStatusCount(
            int online,
            int tempOffline,
            int offline,
            int totalComputers) {
    }

}
