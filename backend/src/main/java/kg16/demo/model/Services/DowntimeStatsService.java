package kg16.demo.model.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import kg16.demo.model.dto.*;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for calculating statistics related to offline event durations and recovery times.
 * Provides insights such as average downtime, downtime distribution buckets, and the worst-performing devices.
 */
@Service
public class DowntimeStatsService {

    private final JdbcTemplate jdbc;
    private final StatisticsUtilsService sus;

    @Autowired
    public DowntimeStatsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sus = new StatisticsUtilsService(jdbc);
    }

    /**
     * Identifies the top 5 most problematic devices based on total accumulated downtime.
     *
     * @param from start of the time range
     * @param to   end of the time range
     * @return list of {@link TopDevice} sorted by total downtime (descending)
     */
    public List<TopDevice> findMostProblematicDevicesBetween(LocalDateTime from, LocalDateTime to) {
        List<Map<String, Object>> rows = sus.fetchOfflineEvents(from, to);

        Map<String, List<Map<String, Object>>> byMac = rows.stream()
                .collect(Collectors.groupingBy(row -> (String) row.get("mac_address")));

        return byMac.entrySet().stream()
                .map(entry -> {
                    String mac = entry.getKey();
                    long totalDowntime = entry.getValue().stream()
                            .mapToLong(row -> sus.calculateDowntimeSeconds(row))
                            .sum();
                    return new TopDevice(mac, entry.getValue().size(), totalDowntime);
                })
                .sorted(Comparator.comparingLong(TopDevice::downtimeSeconds).reversed())
                .limit(5)
                .toList();
    }

    /**
     * Calculates the average downtime (in seconds) for all offline events in the specified range.
     *
     * @param from start of the time range
     * @param to   end of the time range
     * @return optional average downtime (empty if no data)
     */
    public OptionalDouble findAverageDowntimeBetween(LocalDateTime from, LocalDateTime to) {
        return sus.fetchOfflineEvents(from, to).stream()
                .mapToLong(sus::calculateDowntimeSeconds)
                .average();
    }

    /**
     * Groups resolved offline events into predefined duration ranges ("0–10 min", "10–30 min", etc.)
     * to build a downtime histogram.
     *
     * @param from start of the time range
     * @param to   end of the time range
     * @return list of {@link DowntimeBucket} representing the histogram
     */
    public List<DowntimeBucket> getDowntimeHistogram(LocalDateTime from, LocalDateTime to) {
        List<Map<String, Object>> rows = sus.fetchOfflineEvents(from, to);

        Map<String, Integer> buckets = new LinkedHashMap<>(Map.of(
                "0–10 min", 0, "10–30 min", 0, "30–60 min", 0,
                "1–2 h", 0, "2–4 h", 0, "4–8 h", 0, "8+ h", 0
        ));

        for (Map<String, Object> row : rows) {
            Timestamp end = (Timestamp) row.get("restored_at");
            if (end == null) continue; // Skip unresolved events

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

        return StatisticsUtilsService.mapToList(buckets, DowntimeBucket::new);
    }

    /**
     * Returns the 5 longest recovery time events (restored events only) in the given time range.
     *
     * @param from start of the time range
     * @param to   end of the time range
     * @return list of {@link TopRecoveryTimeEvent}, sorted by recovery duration descending
     */
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
            long seconds = sus.calculateDowntimeSeconds(row);
            return new TopRecoveryTimeEvent(
                    (String) row.get("mac_address"),
                    seconds,
                    restored.format(fmt));
        }).toList();
    }
}
