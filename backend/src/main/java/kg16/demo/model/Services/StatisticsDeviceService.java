package kg16.demo.model.services;

import kg16.demo.model.dto.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static kg16.demo.model.services.StatisticsUtilsService.*;

@Service
public class StatisticsDeviceService {

    private final JdbcTemplate jdbc;

    public StatisticsDeviceService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // === Device Stats ===

    public List<TopDevice> findFilteredTopDevices(List<String> tags, List<String> locations, List<String> devices,
                                                  LocalDateTime from, LocalDateTime to) {
        StringBuilder sql = new StringBuilder("SELECT mac_address, offline_since, restored_at FROM OfflineEvents WHERE offline_since BETWEEN ? AND ?");
        List<Object> params = new ArrayList<>(List.of(from, to));
        appendFilterConditions(sql, params, tags, locations, devices);

        List<Map<String, Object>> rows = jdbc.queryForList(sql.toString(), params.toArray());
        Map<String, List<Map<String, Object>>> grouped = rows.stream().collect(Collectors.groupingBy(r -> (String) r.get("mac_address")));

        return grouped.entrySet().stream()
                .map(e -> new TopDevice(e.getKey(), e.getValue().size(),
                        e.getValue().stream().mapToLong(StatisticsUtilsService::calculateDowntimeSeconds).sum()))
                .sorted(Comparator.comparingLong(TopDevice::downtimeSeconds).reversed()).limit(5).toList();
    }

    public OptionalDouble findAverageDowntime(LocalDateTime from, LocalDateTime to) {
        String sql = "SELECT offline_since, restored_at FROM OfflineEvents WHERE offline_since BETWEEN ? AND ?";
        List<Map<String, Object>> rows = jdbc.queryForList(sql, from, to);
        return rows.stream().mapToLong(StatisticsUtilsService::calculateDowntimeSeconds).average();
    }

    public List<DeviceStability> findFilteredDeviceStability(List<String> tags, List<String> locations, List<String> devices,
                                                             LocalDateTime from, LocalDateTime to) {
        StringBuilder sql = new StringBuilder("SELECT mac_address, offline_since, restored_at FROM OfflineEvents WHERE offline_since < ? AND (restored_at IS NULL OR restored_at > ?)");
        List<Object> params = new ArrayList<>(List.of(to, from));
        appendFilterConditions(sql, params, tags, locations, devices);

        List<Map<String, Object>> rows = jdbc.queryForList(sql.toString(), params.toArray());
        long totalSeconds = Duration.between(from, to).getSeconds();
        Map<String, Long> grouped = new HashMap<>();

        for (Map<String, Object> r : rows) {
            String mac = (String) r.get("mac_address");
            LocalDateTime start = ((Timestamp) r.get("offline_since")).toLocalDateTime();
            LocalDateTime end = Optional.ofNullable((Timestamp) r.get("restored_at")).map(Timestamp::toLocalDateTime).orElse(to);
            long downtime = Duration.between(start.isBefore(from) ? from : start, end.isAfter(to) ? to : end).getSeconds();
            grouped.merge(mac, downtime, Long::sum);
        }

        return grouped.entrySet().stream()
                .map(e -> new DeviceStability(e.getKey(), Math.max(0, 100 - 100.0 * e.getValue() / totalSeconds)))
                .sorted(Comparator.comparingDouble(DeviceStability::uptimePercentage).reversed()).limit(10).toList();
    }

    public List<String> findFilteredSilentDevices(LocalDateTime from, LocalDateTime to) {
        String base = """
            SELECT td.mac_address FROM TrackedDevices td
            LEFT JOIN (SELECT DISTINCT mac_address FROM OfflineEvents WHERE offline_since BETWEEN ? AND ?) oe
            ON td.mac_address = oe.mac_address
            WHERE oe.mac_address IS NULL
        """;
        return jdbc.query(base, (rs, rowNum) -> rs.getString("mac_address"), from, to);
    }

    // === Event Stats ===

    public int countEventsInRange(LocalDateTime from, LocalDateTime to) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM OfflineEvents WHERE offline_since BETWEEN ? AND ?", Integer.class, from, to);
    }

    public int countCurrentlyOfflineDevices() {
        String sql = """
            SELECT COUNT(*) FROM (
                SELECT mac_address, MAX(offline_since) AS last_event
                FROM OfflineEvents
                GROUP BY mac_address
            ) latest
            JOIN OfflineEvents oe ON oe.mac_address = latest.mac_address AND oe.offline_since = latest.last_event
            WHERE oe.restored_at IS NULL
        """;
        return jdbc.queryForObject(sql, Integer.class);
    }

    public List<MostNotifiedDevice> findFilteredMostNotifiedDevices(List<String> tags, List<String> locations, List<String> devices,
                                                                     LocalDateTime from, LocalDateTime to) {
        StringBuilder sql = new StringBuilder("""
            SELECT e.mac_address FROM Notifications n
            JOIN OfflineEvents e ON e.event_id = n.event_id
            WHERE e.offline_since BETWEEN ? AND ?
        """);
        List<Object> params = new ArrayList<>(List.of(from, to));
        appendFilterConditions(sql, params, tags, locations, devices);

        List<String> result = jdbc.query(sql.toString(), params.toArray(), (rs, rowNum) -> rs.getString("mac_address"));
        return result.stream().collect(Collectors.groupingBy(mac -> mac, Collectors.counting())).entrySet().stream()
                .map(e -> new MostNotifiedDevice(e.getKey(), e.getValue().intValue()))
                .sorted(Comparator.comparingInt(MostNotifiedDevice::notificationCount).reversed()).toList();
    }
}
