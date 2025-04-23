package kg16.demo.model.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import kg16.demo.model.dto.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LocationStatsService {
    
    private final JdbcTemplate jdbc;

    public LocationStatsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<LocationCount> findEventsByLocationBetween(LocalDateTime from, LocalDateTime to) {
        List<String> locations = jdbc.query("""
            SELECT location FROM OfflineEvents
            WHERE offline_since BETWEEN ? AND ?
        """, (rs, rowNum) -> rs.getString("location"), from, to);

        return StatisticsUtilsService.mapToList(locations.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(loc -> loc, Collectors.counting())),
                LocationCount::new).stream()
                .sorted(Comparator.comparingInt(LocationCount::count).reversed())
                .toList();
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
}
