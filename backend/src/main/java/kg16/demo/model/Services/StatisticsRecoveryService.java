package kg16.demo.model.services;

import kg16.demo.model.dto.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static kg16.demo.model.services.StatisticsUtilsService.*;

@Service
public class StatisticsRecoveryService {

    private final JdbcTemplate jdbc;

    public StatisticsRecoveryService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // === Top Recovery Times ===

    public List<TopRecoveryTimeEvent> findFilteredTopRecoveryEvents(List<String> tags, List<String> locations, List<String> devices,
                                                                     LocalDateTime from, LocalDateTime to) {
        StringBuilder sql = new StringBuilder("SELECT mac_address, offline_since, restored_at FROM OfflineEvents WHERE restored_at IS NOT NULL AND offline_since BETWEEN ? AND ?");
        List<Object> params = new ArrayList<>(List.of(from, to));
        appendFilterConditions(sql, params, tags, locations, devices);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<Map<String, Object>> rows = jdbc.queryForList(sql.toString(), params.toArray());
        return rows.stream()
                .sorted(Comparator.comparingLong(StatisticsUtilsService::calculateDowntimeSeconds).reversed())
                .limit(5)
                .map(r -> new TopRecoveryTimeEvent(
                        (String) r.get("mac_address"),
                        calculateDowntimeSeconds(r),
                        ((Timestamp) r.get("restored_at")).toLocalDateTime().format(fmt)))
                .toList();
    }

    public List<AverageRestoreByLocation> findFilteredAverageRestoreTimes(List<String> tags, List<String> locations, List<String> devices,
                                                                          LocalDateTime from, LocalDateTime to) {
        StringBuilder sql = new StringBuilder("""
            SELECT location, AVG(TIMESTAMPDIFF(SECOND, offline_since, restored_at)) AS avg_seconds
            FROM OfflineEvents
            WHERE restored_at IS NOT NULL AND offline_since BETWEEN ? AND ?
        """);
        List<Object> params = new ArrayList<>(List.of(from, to));
        appendFilterConditions(sql, params, tags, locations, devices);
        sql.append(" GROUP BY location ORDER BY avg_seconds DESC");

        return jdbc.query(sql.toString(), params.toArray(),
                (rs, rowNum) -> new AverageRestoreByLocation(
                        rs.getString("location"),
                        rs.getDouble("avg_seconds")));
    }

    // === Check-in Statistics ===

    public MissedCheckinSummary getMissedCheckinsSummary(LocalDateTime from, LocalDateTime to) {
        int alertThresholdMin = jdbc.queryForObject(
                "SELECT alert_threshold_minutes FROM AdminSettings WHERE id = 1",
                Integer.class);

        LocalDateTime cutoff = to.minusMinutes(alertThresholdMin + 1);

        int totalCheckins = jdbc.queryForObject("""
            SELECT COUNT(*) FROM Checkins
            WHERE last_checkin BETWEEN ? AND ?
        """, Integer.class, from, to);

        int lateCheckins = jdbc.queryForObject("""
            SELECT COUNT(*) FROM Checkins
            WHERE last_checkin BETWEEN ? AND ? AND last_checkin < ?
        """, Integer.class, from, to, cutoff);

        int recentOfflineEvents = jdbc.queryForObject("""
            SELECT COUNT(*) FROM OfflineEvents
            WHERE offline_since BETWEEN ? AND ?
        """, Integer.class, from, to);

        return new MissedCheckinSummary(totalCheckins, lateCheckins, recentOfflineEvents);
    }

    // === Downtime Histogram ===

    public List<DowntimeBucket> findFilteredDowntimeHistogram(List<String> tags, List<String> locations, List<String> devices,
                                                              LocalDateTime from, LocalDateTime to) {
        StringBuilder sql = new StringBuilder("SELECT offline_since, restored_at FROM OfflineEvents WHERE offline_since BETWEEN ? AND ?");
        List<Object> params = new ArrayList<>(List.of(from, to));
        appendFilterConditions(sql, params, tags, locations, devices);

        List<Map<String, Object>> rows = jdbc.queryForList(sql.toString(), params.toArray());
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

    // === Time-based Counts ===

    public List<DayCount> findFilteredDayCounts(List<String> tags, List<String> locations, List<String> devices,
                                                LocalDateTime from, LocalDateTime to) {
        StringBuilder sql = new StringBuilder("SELECT offline_since FROM OfflineEvents WHERE offline_since BETWEEN ? AND ?");
        List<Object> params = new ArrayList<>(List.of(from, to));
        appendFilterConditions(sql, params, tags, locations, devices);

        List<String> result = jdbc.query(sql.toString(), params.toArray(),
                (rs, rowNum) -> rs.getTimestamp("offline_since").toLocalDateTime().toLocalDate().toString());
        Map<String, Long> grouped = result.stream().collect(Collectors.groupingBy(d -> d, Collectors.counting()));
        return mapToList(grouped, DayCount::new);
    }

    public List<HourCount> findFilteredHourCounts(List<String> tags, List<String> locations, List<String> devices,
                                                  LocalDateTime from, LocalDateTime to) {
        StringBuilder sql = new StringBuilder("SELECT offline_since FROM OfflineEvents WHERE offline_since BETWEEN ? AND ?");
        List<Object> params = new ArrayList<>(List.of(from, to));
        appendFilterConditions(sql, params, tags, locations, devices);

        List<Integer> result = jdbc.query(sql.toString(), params.toArray(),
                (rs, rowNum) -> rs.getTimestamp("offline_since").toLocalDateTime().getHour());
        Map<Integer, Long> grouped = result.stream().collect(Collectors.groupingBy(h -> h, Collectors.counting()));
        return mapToList(grouped, HourCount::new);
    }

    public List<WeekdayCount> findFilteredWeekdayCounts(List<String> tags, List<String> locations, List<String> devices,
                                                        LocalDateTime from, LocalDateTime to) {
        StringBuilder sql = new StringBuilder("SELECT offline_since FROM OfflineEvents WHERE offline_since BETWEEN ? AND ?");
        List<Object> params = new ArrayList<>(List.of(from, to));
        appendFilterConditions(sql, params, tags, locations, devices);

        List<DayOfWeek> result = jdbc.query(sql.toString(), params.toArray(),
                (rs, rowNum) -> rs.getTimestamp("offline_since").toLocalDateTime().getDayOfWeek());
        Map<DayOfWeek, Long> grouped = result.stream().collect(Collectors.groupingBy(d -> d, Collectors.counting()));
        return Arrays.stream(DayOfWeek.values())
                .map(d -> new WeekdayCount(d.name(), grouped.getOrDefault(d, 0L).intValue())).toList();
    }
}
