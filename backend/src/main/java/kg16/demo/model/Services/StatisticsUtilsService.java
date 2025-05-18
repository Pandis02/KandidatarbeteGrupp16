package kg16.demo.model.services;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Utility service for common statistical operations on offline events.
 * Provides shared methods to fetch event data, calculate downtimes, and map datasets into DTOs.
 */
@Service
public class StatisticsUtilsService {

    private final JdbcTemplate jdbc;

    public StatisticsUtilsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Converts a {@code Map<K, Number>} into a {@code List<V>} by applying a mapping function.
     * Primarily used for transforming grouped counts into DTO records like LocationCount or DayCount.
     *
     * @param map         the map of key → numeric count
     * @param constructor the mapper to convert each key-count pair into a result object
     * @param <K>         the key type (e.g., String)
     * @param <V>         the result object type
     * @return list of mapped result objects
     */
    public static <K, V> List<V> mapToList(Map<K, ? extends Number> map, BiMapper<K, Integer, V> constructor) {
        return map.entrySet().stream()
                .map(e -> constructor.map(e.getKey(), e.getValue().intValue()))
                .toList();
    }

    /**
     * Retrieves all offline events within the specified time range.
     *
     * @param from the start timestamp (inclusive)
     * @param to   the end timestamp (inclusive)
     * @return list of raw result rows as key-value maps
     */
    public List<Map<String, Object>> fetchOfflineEvents(LocalDateTime from, LocalDateTime to) {
        return jdbc.queryForList("""
            SELECT mac_address, offline_since, restored_at
            FROM OfflineEvents
            WHERE offline_since BETWEEN ? AND ?
        """, from, to);
    }

    /**
     * Fetches all timestamps for a given column (e.g., "offline_since") between two time boundaries.
     *
     * @param column the column name to retrieve (must be of timestamp type)
     * @param from   the start timestamp
     * @param to     the end timestamp
     * @return list of {@link LocalDateTime} values
     */
    public List<LocalDateTime> fetchTimestamps(String column, LocalDateTime from, LocalDateTime to) {
        String sql = "SELECT " + column + " FROM OfflineEvents WHERE " + column + " BETWEEN ? AND ?";
        return jdbc.query(sql, (rs, rowNum) -> rs.getTimestamp(column).toLocalDateTime(), from, to);
    }

    /**
     * Calculates the downtime in seconds between {@code offline_since} and {@code restored_at}.
     * If {@code restored_at} is null, uses the current time instead.
     *
     * @param row a result row containing the columns {@code offline_since} and {@code restored_at}
     * @return downtime duration in seconds
     */
    public long calculateDowntimeSeconds(Map<String, Object> row) {
        LocalDateTime from = ((Timestamp) row.get("offline_since")).toLocalDateTime();
        Timestamp restoredTs = (Timestamp) row.get("restored_at");
        LocalDateTime to = (restoredTs != null) ? restoredTs.toLocalDateTime() : LocalDateTime.now();
        return Duration.between(from, to).getSeconds();
    }

    /**
     * Functional interface used to convert a key-value pair into an object.
     * Typically used in conjunction with {@link #mapToList}.
     *
     * @param <A> first argument type (e.g., key)
     * @param <B> second argument type (e.g., count)
     * @param <R> return type (e.g., DTO)
     */
    @FunctionalInterface
    interface BiMapper<A, B, R> {
        R map(A a, B b);
    }
}
