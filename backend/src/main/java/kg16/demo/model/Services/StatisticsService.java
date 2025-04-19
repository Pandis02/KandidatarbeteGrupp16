package kg16.demo.model.services;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import kg16.demo.model.services.StatisticsService.BiMapper;
import kg16.demo.model.dto.*;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final JdbcTemplate jdbc;

    public StatisticsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // =========================
    // === BASIC STATISTICS ===
    // =========================

    public List<DayCount> findEventsByDayBetween(LocalDateTime from, LocalDateTime to) {
        List<LocalDateTime> timestamps = fetchTimestamps("offline_since", from, to);
        Map<String, Long> counts = timestamps.stream()
                .map(t -> t.toLocalDate().toString())
                .collect(Collectors.groupingBy(date -> date, Collectors.counting()));

        return mapToList(counts, DayCount::new);
    }

    public List<HourCount> findEventsByHourBetween(LocalDateTime from, LocalDateTime to) {
        List<LocalDateTime> timestamps = fetchTimestamps("offline_since", from, to);
        Map<Integer, Long> counts = timestamps.stream()
                .map(LocalDateTime::getHour)
                .collect(Collectors.groupingBy(hour -> hour, Collectors.counting()));

        return mapToList(counts, HourCount::new);
    }

    public int countEventsBetween(LocalDateTime from, LocalDateTime to) {
        String sql = "SELECT COUNT(*) FROM OfflineEvents WHERE offline_since BETWEEN ? AND ?";
        return jdbc.queryForObject(sql, Integer.class, from, to);
    }

    public int countCurrentlyOfflineDevices() {
        return jdbc.queryForObject("""
            SELECT COUNT(DISTINCT mac_address)
            FROM OfflineEvents
            WHERE restored_at IS NULL
        """, Integer.class);
    }

    // ================================
    // === TAGS & LOCATIONS & TRENDS ===
    // ================================

    public List<TagCount> findCommonTagsBetween(LocalDateTime from, LocalDateTime to) {
        List<String> tags = jdbc.query("""
            SELECT tag FROM OfflineEvents
            WHERE tag IS NOT NULL AND offline_since BETWEEN ? AND ?
        """, (rs, rowNum) -> rs.getString("tag"), from, to);

        return mapToList(tags.stream()
                .collect(Collectors.groupingBy(tag -> tag, Collectors.counting())),
                TagCount::new).stream()
                .sorted(Comparator.comparingInt(TagCount::count).reversed())
                .toList();
    }

    public List<LocationCount> findEventsByLocationBetween(LocalDateTime from, LocalDateTime to) {
        List<String> locations = jdbc.query("""
            SELECT location FROM OfflineEvents
            WHERE offline_since BETWEEN ? AND ?
        """, (rs, rowNum) -> rs.getString("location"), from, to);

        return mapToList(locations.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(loc -> loc, Collectors.counting())),
                LocationCount::new).stream()
                .sorted(Comparator.comparingInt(LocationCount::count).reversed())
                .toList();
    }

    public List<TagTrend> getTagTrends(LocalDateTime from, LocalDateTime to) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
            SELECT offline_since, tag FROM OfflineEvents
            WHERE tag IS NOT NULL AND offline_since BETWEEN ? AND ?
        """, from, to);

        Map<String, Map<String, Integer>> grouped = new TreeMap<>();
        for (Map<String, Object> row : rows) {
            String date = ((Timestamp) row.get("offline_since")).toLocalDateTime().toLocalDate().toString();
            String tag = (String) row.get("tag");

            grouped.computeIfAbsent(date, d -> new HashMap<>())
                   .merge(tag, 1, Integer::sum);
        }

        List<TagTrend> trends = new ArrayList<>();
        grouped.forEach((date, tagMap) ->
            tagMap.forEach((tag, count) -> trends.add(new TagTrend(date, tag, count)))
        );
        return trends;
    }

    // ===============================
    // === TOP DEVICES / RECOVERY ===
    // ===============================

    public List<TopDevice> findMostProblematicDevicesBetween(LocalDateTime from, LocalDateTime to) {
        List<Map<String, Object>> rows = fetchOfflineEvents(from, to);

        Map<String, List<Map<String, Object>>> byMac = rows.stream()
                .collect(Collectors.groupingBy(row -> (String) row.get("mac_address")));

        return byMac.entrySet().stream()
                .map(entry -> {
                    String mac = entry.getKey();
                    long totalDowntime = entry.getValue().stream()
                            .mapToLong(this::calculateDowntimeSeconds)
                            .sum();
                    return new TopDevice(mac, entry.getValue().size(), totalDowntime);
                })
                .sorted(Comparator.comparingLong(TopDevice::downtimeSeconds).reversed())
                .limit(5)
                .toList();
    }

    public OptionalDouble findAverageDowntimeBetween(LocalDateTime from, LocalDateTime to) {
        return fetchOfflineEvents(from, to).stream()
                .mapToLong(this::calculateDowntimeSeconds)
                .average();
    }

    public List<TopRecoveryTimeEvent> findTopRecoveryTimeEvents(LocalDateTime from, LocalDateTime to) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
            SELECT mac_address, offline_since, restored_at
            FROM OfflineEvents
            WHERE restored_at IS NOT NULL AND offline_since BETWEEN ? AND ?
            ORDER BY TIMESTAMPDIFF(SECOND, offline_since, restored_at) DESC
            LIMIT 5
        """, from, to);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return rows.stream().map(row -> {
            LocalDateTime restored = ((Timestamp) row.get("restored_at")).toLocalDateTime();
            long seconds = calculateDowntimeSeconds(row);
            return new TopRecoveryTimeEvent(
                    (String) row.get("mac_address"),
                    seconds,
                    restored.format(fmt));
        }).toList();
    }

    // ============================
    // === WEEKDAY / HISTOGRAM ===
    // ============================

    public List<WeekdayCount> findEventCountByWeekday(LocalDateTime from, LocalDateTime to) {
        List<LocalDateTime> timestamps = fetchTimestamps("offline_since", from, to);

        Map<DayOfWeek, Long> counts = timestamps.stream()
                .collect(Collectors.groupingBy(LocalDateTime::getDayOfWeek, Collectors.counting()));

        return Arrays.stream(DayOfWeek.values())
                .map(day -> new WeekdayCount(day.toString(), counts.getOrDefault(day, 0L).intValue()))
                .toList();
    }

    public List<DowntimeBucket> getDowntimeHistogram(LocalDateTime from, LocalDateTime to) {
        List<Map<String, Object>> rows = fetchOfflineEvents(from, to);

        Map<String, Integer> buckets = new LinkedHashMap<>(Map.of(
                "0–10 min", 0, "10–30 min", 0, "30–60 min", 0,
                "1–2 h", 0, "2–4 h", 0, "4–8 h", 0, "8+ h", 0
        ));

        for (Map<String, Object> row : rows) {
            Timestamp end = (Timestamp) row.get("restored_at");
            if (end == null) continue;

            long min = Duration.between(
                    ((Timestamp) row.get("offline_since")).toLocalDateTime(),
                    end.toLocalDateTime()
            ).toMinutes();

            String label;
if (min <= 10) label = "0–10 min";
else if (min <= 30) label = "10–30 min";
else if (min <= 60) label = "30–60 min";
else if (min <= 120) label = "1–2 h";
else if (min <= 240) label = "2–4 h";
else if (min <= 480) label = "4–8 h";
else label = "8+ h";


            buckets.merge(label, 1, Integer::sum);
        }

        return mapToList(buckets, DowntimeBucket::new);
    }

    public List<MostNotifiedDevice> findMostNotifiedDevices(LocalDateTime from, LocalDateTime to) {
        String sql = """
            SELECT e.mac_address, COUNT(*) AS notification_count
            FROM Notifications n
            JOIN OfflineEvents e ON n.event_id = e.event_id
            WHERE e.offline_since BETWEEN ? AND ?
            GROUP BY e.mac_address
            ORDER BY notification_count DESC
        """;
    
        return jdbc.query(sql, (rs, rowNum) ->
            new MostNotifiedDevice(
                rs.getString("mac_address"),
                rs.getInt("notification_count")
            ), from, to
        );
    }
    
    

    public List<AverageRestoreByLocation> findAverageRestoreTimesPerLocation(LocalDateTime from, LocalDateTime to) {
        String sql = """
            SELECT location, AVG(TIMESTAMPDIFF(SECOND, offline_since, restored_at)) AS avg_seconds
            FROM OfflineEvents
            WHERE restored_at IS NOT NULL AND offline_since BETWEEN ? AND ?
            GROUP BY location
            ORDER BY avg_seconds DESC
        """;
    
        return jdbc.query(sql, (rs, rowNum) -> new AverageRestoreByLocation(
            rs.getString("location"),
            rs.getDouble("avg_seconds")
        ), from, to);
    }

    public List<NotificationChannelCount> getNotificationChannelDistributionBetween(LocalDateTime from, LocalDateTime to) {
        String sql = """
            SELECT r.recipient_type, COUNT(*) AS count
            FROM Notifications n
            JOIN OfflineEvents e ON n.event_id = e.event_id
            JOIN NotificationRecipients nr ON nr.notification_id = n.notification_id
            JOIN Recipients r ON r.recipient_value = nr.recipient_value
            WHERE e.offline_since BETWEEN ? AND ?
            GROUP BY r.recipient_type
        """;
    
        return jdbc.query(sql, (rs, rowNum) ->
            new NotificationChannelCount(
                rs.getString("recipient_type"),
                rs.getInt("count")
            ), from, to
        );
    }
    
    

    public MissedCheckinSummary getMissedCheckinStatsBetween(LocalDateTime from, LocalDateTime to) {
        // Tröskelvärde från inställningar
        int alertThresholdMin = jdbc.queryForObject(
            "SELECT alert_threshold_minutes FROM AdminSettings WHERE id = 1",
            Integer.class
        );
        int gracePeriodMinutes = alertThresholdMin + 1;
    
        LocalDateTime lateCutoff = LocalDateTime.now().minusMinutes(gracePeriodMinutes);
    
        // Antal checkins i valt intervall
        int totalCheckins = jdbc.queryForObject("""
            SELECT COUNT(*) FROM Checkins
            WHERE last_checkin BETWEEN ? AND ?
        """, Integer.class, from, to);
    
        // Hur många var sena (baserat på nuvarande cutoff)
        int lateCheckins = jdbc.queryForObject("""
            SELECT COUNT(*) FROM Checkins
            WHERE last_checkin BETWEEN ? AND ? AND last_checkin < ?
        """, Integer.class, from, to, lateCutoff);
    
        // Offline events under perioden
        int recentOfflineEvents = jdbc.queryForObject("""
            SELECT COUNT(*) FROM OfflineEvents
            WHERE offline_since BETWEEN ? AND ?
        """, Integer.class, from, to);
    
        return new MissedCheckinSummary(totalCheckins, lateCheckins, recentOfflineEvents);
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

    

    // ===================
    // === UTILITIES  ===
    // ===================

    private List<Map<String, Object>> fetchOfflineEvents(LocalDateTime from, LocalDateTime to) {
        return jdbc.queryForList("""
            SELECT mac_address, offline_since, restored_at
            FROM OfflineEvents
            WHERE offline_since BETWEEN ? AND ?
        """, from, to);
    }

    private List<LocalDateTime> fetchTimestamps(String column, LocalDateTime from, LocalDateTime to) {
        String sql = "SELECT " + column + " FROM OfflineEvents WHERE " + column + " BETWEEN ? AND ?";
        return jdbc.query(sql, (rs, rowNum) -> rs.getTimestamp(column).toLocalDateTime(), from, to);
    }

    private long calculateDowntimeSeconds(Map<String, Object> row) {
        LocalDateTime from = ((Timestamp) row.get("offline_since")).toLocalDateTime();
        Timestamp restoredTs = (Timestamp) row.get("restored_at");
        LocalDateTime to = (restoredTs != null) ? restoredTs.toLocalDateTime() : LocalDateTime.now();
        return Duration.between(from, to).getSeconds();
    }

    private <K, V> List<V> mapToList(Map<K, ? extends Number> map, BiMapper<K, Integer, V> constructor) {
        return map.entrySet().stream()
            .map(e -> constructor.map(e.getKey(), e.getValue().intValue()))
            .toList();
    }
    

    @FunctionalInterface
    interface BiMapper<A, B, R> {
        R map(A a, B b);
    }
}
