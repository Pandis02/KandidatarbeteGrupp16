package kg16.demo.model.services;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import kg16.demo.model.dto.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BasicStatsService {

    private final JdbcTemplate jdbc;
    private final StatisticsUtilsService sus;

    public BasicStatsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sus = new StatisticsUtilsService(jdbc);
    }

    public List<DayCount> findEventsByDayBetween(LocalDateTime from, LocalDateTime to) {
        List<LocalDateTime> timestamps = sus.fetchTimestamps("offline_since", from, to);
        Map<String, Long> counts = timestamps.stream()
                .map(t -> t.toLocalDate().toString())
                .collect(Collectors.groupingBy(date -> date, Collectors.counting()));

        return StatisticsUtilsService.mapToList(counts, DayCount::new);
    }

    public List<HourCount> findEventsByHourBetween(LocalDateTime from, LocalDateTime to) {
        List<LocalDateTime> timestamps = sus.fetchTimestamps("offline_since", from, to);
        Map<Integer, Long> counts = timestamps.stream()
                .map(LocalDateTime::getHour)
                .collect(Collectors.groupingBy(hour -> hour, Collectors.counting()));

        return StatisticsUtilsService.mapToList(counts, HourCount::new);
    }

    public List<WeekdayCount> findEventCountByWeekday(LocalDateTime from, LocalDateTime to) {
        List<LocalDateTime> timestamps = sus.fetchTimestamps("offline_since", from, to);

        Map<DayOfWeek, Long> counts = timestamps.stream()
                .collect(Collectors.groupingBy(LocalDateTime::getDayOfWeek, Collectors.counting()));

        return Arrays.stream(DayOfWeek.values())
                .map(day -> new WeekdayCount(day.toString(), counts.getOrDefault(day, 0L).intValue()))
                .toList();
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

}
