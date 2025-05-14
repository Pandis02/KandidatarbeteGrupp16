package kg16.demo.model.services;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import kg16.demo.model.dto.*;

import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for computing statistics related to event tags.
 * Includes frequency counts of tags and their distribution over time (trend analysis).
 */
@Service
public class TagStatsService {

    private final JdbcTemplate jdbc;

    /**
     * Constructs the service with a provided JDBC template.
     *
     * @param jdbc the JDBC template used for executing queries
     */
    public TagStatsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Retrieves a list of tag frequencies for events that occurred in the specified time range.
     * Tags are grouped and counted, then sorted by most frequent first.
     *
     * @param from the start of the time range
     * @param to   the end of the time range
     * @return list of {@link TagCount} records
     */
    public List<TagCount> findCommonTagsBetween(LocalDateTime from, LocalDateTime to) {
        List<String> tags = jdbc.query("""
            SELECT tag FROM OfflineEvents
            WHERE tag IS NOT NULL AND offline_since BETWEEN ? AND ?
        """, (rs, rowNum) -> rs.getString("tag"), from, to);

        return StatisticsUtilsService.mapToList(
                tags.stream()
                    .collect(Collectors.groupingBy(tag -> tag, Collectors.counting())),
                TagCount::new
            ).stream()
            .sorted(Comparator.comparingInt(TagCount::count).reversed())
            .toList();
    }

    /**
     * Builds a daily trend showing how the frequency of each tag changes over time
     * within the specified range. Grouping is done per day and tag.
     *
     * @param from the start of the time range
     * @param to   the end of the time range
     * @return list of {@link TagTrend} records with date-tag-count combinations
     */
    public List<TagTrend> getTagTrends(LocalDateTime from, LocalDateTime to) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
            SELECT offline_since, tag FROM OfflineEvents
            WHERE tag IS NOT NULL AND offline_since BETWEEN ? AND ?
        """, from, to);

        Map<String, Map<String, Integer>> grouped = new TreeMap<>();
        for (Map<String, Object> row : rows) {
            String date = ((Timestamp) row.get("offline_since"))
                            .toLocalDateTime()
                            .toLocalDate()
                            .toString();
            String tag = (String) row.get("tag");

            grouped.computeIfAbsent(date, d -> new HashMap<>())
                   .merge(tag, 1, Integer::sum);
        }

        List<TagTrend> trends = new ArrayList<>();
        grouped.forEach((date, tagMap) ->
            tagMap.forEach((tag, count) ->
                trends.add(new TagTrend(date, tag, count))
            )
        );
        return trends;
    }
}
