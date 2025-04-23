package kg16.demo.model.services;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import kg16.demo.model.dto.*;

import java.sql.Timestamp;
import java.time.*;
import java.util.*;

@Service
public class DeviceStatsService {
    
    private final JdbcTemplate jdbc;

    public DeviceStatsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<DeviceStability> computeDeviceStability(LocalDateTime from, LocalDateTime to) {
        long totalSeconds = Duration.between(from, to).getSeconds();
    
        String sql = """
            SELECT mac_address, offline_since, restored_at
            FROM OfflineEvents
            WHERE offline_since < ? AND (restored_at IS NULL OR restored_at > ?)
        """;
    
        List<Map<String, Object>> rows = jdbc.queryForList(sql, to, from);
    
        Map<String, Long> downtimePerDevice = new HashMap<>();
    
        for (Map<String, Object> row : rows) {
            String mac = (String) row.get("mac_address");
            LocalDateTime start = ((Timestamp) row.get("offline_since")).toLocalDateTime();
            Timestamp restoredTs = (Timestamp) row.get("restored_at");
    
            LocalDateTime end = (restoredTs != null) ? restoredTs.toLocalDateTime() : to;
    
            // Clamp values to the selected range
            LocalDateTime effectiveStart = start.isBefore(from) ? from : start;
            LocalDateTime effectiveEnd = end.isAfter(to) ? to : end;
    
            if (effectiveEnd.isAfter(effectiveStart)) {
                long downtime = Duration.between(effectiveStart, effectiveEnd).getSeconds();
                downtimePerDevice.merge(mac, downtime, Long::sum);
            }
        }
    
        return downtimePerDevice.entrySet().stream()
            .map(entry -> {
                long downtime = entry.getValue();
                double uptimePct = 100.0 * (1.0 - (double) downtime / totalSeconds);
                return new DeviceStability(entry.getKey(), Math.max(0.0, Math.min(100.0, uptimePct)));
            })
            .sorted(Comparator.comparingDouble(DeviceStability::uptimePercentage).reversed())
            .limit(10)
            .toList();
    }

    public List<String> findDevicesWithNoEventsBetween(LocalDateTime from, LocalDateTime to) {
        String sql = """
            SELECT td.mac_address
            FROM TrackedDevices td
            LEFT JOIN (
                SELECT DISTINCT mac_address
                FROM OfflineEvents
                WHERE offline_since BETWEEN ? AND ?
            ) oe ON td.mac_address = oe.mac_address
            WHERE oe.mac_address IS NULL
            ORDER BY td.mac_address
        """;

        return jdbc.query(sql, (rs, rowNum) -> rs.getString("mac_address"), from, to);
    }
 
}
