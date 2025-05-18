package kg16.demo.model.services;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import kg16.demo.model.dto.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for calculating location-based statistics from offline event data.
 * Includes total event counts by location and average recovery durations.
 */
@Service
public class LocationStatsService {
    
    private final JdbcTemplate jdbc;

    /**
     * Constructs the service using the provided JDBC template.
     *
     * @param jdbc the JdbcTemplate used for querying event data
     */
    public LocationStatsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Returns a list of locations with the number of offline events that occurred in each
     * during the given time range. The list is sorted in descending order of event count.
     *
     * @param from start of the time range
     * @param to   end of the time range
     * @return list of {@link LocationCount} records
     */
    public List<LocationCount> findEventsByLocationBetween(LocalDateTime from, LocalDateTime to) {
        List<String> locations = jdbc.query("""
            SELECT location FROM OfflineEvents
            WHERE offline_since BETWEEN ? AND ?
        """, (rs, rowNum) -> rs.getString("location"), from, to);

        return StatisticsUtilsService.mapToList(
                locations.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(loc -> loc, Collectors.counting())),
                LocationCount::new
            ).stream()
            .sorted(Comparator.comparingInt(LocationCount::count).reversed())
            .toList();
    }

    /**
     * Calculates the average restore (recovery) time in seconds for each location
     * during the specified time period. Only resolved events (with non-null restored_at)
     * are included.
     *
     * @param from start of the time range
     * @param to   end of the time range
     * @return list of {@link AverageRestoreByLocation} records sorted by average duration (descending)
     */
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
}
