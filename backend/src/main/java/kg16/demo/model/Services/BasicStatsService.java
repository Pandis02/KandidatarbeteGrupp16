package kg16.demo.model.services;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import kg16.demo.model.dto.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for computing basic statistics about offline events.
 * Provides insights like event counts by day, hour, weekday, and current offline device status.
 * Delegates raw timestamp fetching to {@link StatisticsUtilsService}.
 */
@Service
public class BasicStatsService {

    private final JdbcTemplate jdbc;
    private final StatisticsUtilsService sus;

    /**
     * Constructs a BasicStatsService using the provided JDBC connection.
     *
     * @param jdbc the JdbcTemplate used for database queries
     */
    public BasicStatsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sus = new StatisticsUtilsService(jdbc);
    }

    /**
     * Returns a list of event counts grouped by calendar day within a time range.
     *
     * @param from start timestamp
     * @param to   end timestamp
     * @return list of {@link DayCount} with one entry per day
     */
    public List<DayCount> findEventsByDayBetween(LocalDateTime from, LocalDateTime to) {
        List<LocalDateTime> timestamps = sus.fetchTimestamps("offline_since", from, to);
        Map<String, Long> counts = timestamps.stream()
                .map(t -> t.toLocalDate().toString())
                .collect(Collectors.groupingBy(date -> date, Collectors.counting()));

        return StatisticsUtilsService.mapToList(counts, DayCount::new);
    }

    /**
     * Returns a list of event counts grouped by hour of the day (0–23) within a time range.
     *
     * @param from start timestamp
     * @param to   end timestamp
     * @return list of {@link HourCount} with one entry per hour
     */
    public List<HourCount> findEventsByHourBetween(LocalDateTime from, LocalDateTime to) {
        List<LocalDateTime> timestamps = sus.fetchTimestamps("offline_since", from, to);
        Map<Integer, Long> counts = timestamps.stream()
                .map(LocalDateTime::getHour)
                .collect(Collectors.groupingBy(hour -> hour, Collectors.counting()));

        return StatisticsUtilsService.mapToList(counts, HourCount::new);
    }

    /**
     * Returns a list of event counts grouped by weekday (Monday–Sunday) within a time range.
     *
     * @param from start timestamp
     * @param to   end timestamp
     * @return list of {@link WeekdayCount} ordered from Monday to Sunday
     */
    public List<WeekdayCount> findEventCountByWeekday(LocalDateTime from, LocalDateTime to) {
        List<LocalDateTime> timestamps = sus.fetchTimestamps("offline_since", from, to);

        Map<DayOfWeek, Long> counts = timestamps.stream()
                .collect(Collectors.groupingBy(LocalDateTime::getDayOfWeek, Collectors.counting()));

        return Arrays.stream(DayOfWeek.values())
                .map(day -> new WeekdayCount(day.toString(), counts.getOrDefault(day, 0L).intValue()))
                .toList();
    }

    /**
     * Counts the number of offline events recorded between the given timestamps.
     *
     * @param from start timestamp
     * @param to   end timestamp
     * @return total number of events in the range
     */
    public int countEventsBetween(LocalDateTime from, LocalDateTime to) {
        String sql = "SELECT COUNT(*) FROM OfflineEvents WHERE offline_since BETWEEN ? AND ?";
        return jdbc.queryForObject(sql, Integer.class, from, to);
    }

    /**
     * Returns the number of devices that are currently offline.
     * A device is considered offline if its most recent event has no restore time.
     *
     * @return number of unique MAC addresses currently offline
     */
    public int countCurrentlyOfflineDevices() {
        return jdbc.queryForObject("""
            SELECT COUNT(DISTINCT mac_address)
            FROM OfflineEvents
            WHERE restored_at IS NULL
        """, Integer.class);
    }
}
