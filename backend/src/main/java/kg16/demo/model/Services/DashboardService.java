package kg16.demo.model.services;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final JdbcTemplate jdbc;
    private final AdminSettingsService settings;

    public DashboardService(AdminSettingsService settings, JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
        this.settings = settings;
    }

    public DeviceStatusCount getCounts() {
        var minutes = settings.getSettings().getAlertThresholdMinutes();
        String sql = """
                SELECT
                    COUNT(td.mac_address) AS total,
                    COUNT(oe.mac_address) AS total_unreachable_devices_prior_alert_threshold,
                    COUNT(DISTINCT oe2.mac_address) AS total_unreachable_devices_past_alert_threshold
                FROM
                    TrackedDevices td
                LEFT JOIN
                    OfflineEvents oe ON td.mac_address = oe.mac_address AND oe.restored_at IS NULL AND oe.offline_since > NOW() - INTERVAL '%d' MINUTE
                LEFT JOIN
                    OfflineEvents oe2 ON td.mac_address = oe2.mac_address AND oe2.restored_at IS NULL AND oe2.offline_since < NOW() - INTERVAL '%d' MINUTE
                WHERE
                    td.enabled = TRUE;
                """;

        return jdbc.queryForObject(String.format(sql, minutes, minutes), (r, rowNum) -> {
            return new DeviceStatusCount(
                    r.getInt("total_unreachable_devices_prior_alert_threshold"),
                    r.getInt("total_unreachable_devices_past_alert_threshold"),
                    r.getInt("total"),
                    r.getInt("total") -
                            r.getInt("total_unreachable_devices_prior_alert_threshold") -
                            r.getInt("total_unreachable_devices_past_alert_threshold"));
        });
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
            int tempOffline,
            int offline,
            int totalComputers,
            int online) {
    }

}
