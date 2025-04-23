package kg16.demo.model.services;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import kg16.demo.model.dto.*;

import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TagStatsService {
    
    private final JdbcTemplate jdbc;

    public TagStatsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<TagCount> findCommonTagsBetween(LocalDateTime from, LocalDateTime to) {
        List<String> tags = jdbc.query("""
            SELECT tag FROM OfflineEvents
            WHERE tag IS NOT NULL AND offline_since BETWEEN ? AND ?
        """, (rs, rowNum) -> rs.getString("tag"), from, to);

        return StatisticsUtilsService.mapToList(tags.stream()
                .collect(Collectors.groupingBy(tag -> tag, Collectors.counting())),
                TagCount::new).stream()
                .sorted(Comparator.comparingInt(TagCount::count).reversed())
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

}
