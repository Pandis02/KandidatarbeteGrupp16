package kg16.demo.model.services;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class StatisticsUtilsService {

    private final JdbcTemplate jdbc;

    public StatisticsUtilsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }


    public static <K, V> List<V> mapToList(Map<K, ? extends Number> map, BiMapper<K, Integer, V> constructor) {
        return map.entrySet().stream()
                .map(e -> constructor.map(e.getKey(), e.getValue().intValue()))
                .toList();
    }

    public List<Map<String, Object>> fetchOfflineEvents(LocalDateTime from, LocalDateTime to) {
        return jdbc.queryForList("""
            SELECT mac_address, offline_since, restored_at
            FROM OfflineEvents
            WHERE offline_since BETWEEN ? AND ?
        """, from, to);
    }

    public List<LocalDateTime> fetchTimestamps(String column, LocalDateTime from, LocalDateTime to) {
        String sql = "SELECT " + column + " FROM OfflineEvents WHERE " + column + " BETWEEN ? AND ?";
        return jdbc.query(sql, (rs, rowNum) -> rs.getTimestamp(column).toLocalDateTime(), from, to);
    }

    public long calculateDowntimeSeconds(Map<String, Object> row) {
        LocalDateTime from = ((Timestamp) row.get("offline_since")).toLocalDateTime();
        Timestamp restoredTs = (Timestamp) row.get("restored_at");
        LocalDateTime to = (restoredTs != null) ? restoredTs.toLocalDateTime() : LocalDateTime.now();
        return Duration.between(from, to).getSeconds();
    }

    @FunctionalInterface
    interface BiMapper<A, B, R> {
        R map(A a, B b);
    }

}
