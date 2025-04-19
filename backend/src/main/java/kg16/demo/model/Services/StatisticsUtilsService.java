package kg16.demo.model.services;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class StatisticsUtilsService {

    public static long calculateDowntimeSeconds(Map<String, Object> row) {
        Timestamp from = (Timestamp) row.get("offline_since");
        Timestamp to = (Timestamp) row.get("restored_at");
        if (from == null) return 0;
        return Duration.between(from.toLocalDateTime(),
                to != null ? to.toLocalDateTime() : LocalDateTime.now()).getSeconds();
    }

    public static <K, V> List<V> mapToList(Map<K, ? extends Number> map, StatisticsService.BiMapper<K, Integer, V> constructor) {
        return map.entrySet().stream()
                .map(e -> constructor.map(e.getKey(), e.getValue().intValue()))
                .toList();
    }

    public static void appendFilterConditions(StringBuilder sql, List<Object> params,
                                              List<String> tags, List<String> locations, List<String> devices) {
        if (tags != null && !tags.isEmpty()) {
            sql.append(" AND tag IN (").append(String.join(",", Collections.nCopies(tags.size(), "?"))).append(")");
            params.addAll(tags);
        }
        if (locations != null && !locations.isEmpty()) {
            sql.append(" AND location IN (").append(String.join(",", Collections.nCopies(locations.size(), "?"))).append(")");
            params.addAll(locations);
        }
        if (devices != null && !devices.isEmpty()) {
            sql.append(" AND mac_address IN (").append(String.join(",", Collections.nCopies(devices.size(), "?"))).append(")");
            params.addAll(devices);
        }
    }
}
