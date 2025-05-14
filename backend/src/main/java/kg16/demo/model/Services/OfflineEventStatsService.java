package kg16.demo.model.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import kg16.demo.model.dto.FalsePositiveSummary;

/**
 * Service responsible for estimating false positives among currently ongoing offline events.
 * It uses tag- and location-based probabilities, either predefined or learned from historical data,
 * to determine the likelihood that an offline event is not a true fault.
 */
@Service
public class OfflineEventStatsService {

    private final JdbcTemplate jdbc;

    /**
     * Base fallback false positive rates if no confirmed historical data is available.
     */
    private static final Map<String, Double> BASE_TAG_FALSE_POSITIVE_RATES = Map.of(
        "Power Outage", 0.9,
        "Network Issue", 0.8,
        "Maintenance", 0.95,
        "Hardware Removed", 0.1,
        "Unknown", 0.2
    );

    public OfflineEventStatsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Computes a summary of ongoing offline events and estimates how many are likely false positives.
     * It uses either learned rates (from confirmed events) or predefined fallback rates.
     *
     * @return {@link FalsePositiveSummary} containing counts, tag data, and FP estimation
     */
    public FalsePositiveSummary getCurrentMissedCheckinStats() {
        int ongoingOfflineEvents = jdbc.queryForObject("""
            SELECT COUNT(*) FROM OfflineEvents
            WHERE restored_at IS NULL
        """, Integer.class);

        Map<String, Integer> historicalTags = new HashMap<>();
        jdbc.query("""
            SELECT tag, COUNT(*) AS tag_count FROM OfflineEvents
            GROUP BY tag
        """, rs -> {
            String tag = normalizeTag(rs.getString("tag"));
            int count = rs.getInt("tag_count");
            historicalTags.put(tag, count);
        });

        boolean hasEnoughHistoricalData = hasSufficientHistoricalData();

        Map<String, Double> tagFalsePositiveRates = hasEnoughHistoricalData
            ? computeLearnedTagFalsePositiveRates()
            : computeFalsePositiveRates(historicalTags, BASE_TAG_FALSE_POSITIVE_RATES);

        tagFalsePositiveRates.putIfAbsent("Unknown", BASE_TAG_FALSE_POSITIVE_RATES.get("Unknown"));

        Map<String, Double> locationFalsePositiveRates = hasEnoughHistoricalData
            ? computeLearnedLocationFalsePositiveRates()
            : new HashMap<>();

        boolean locationRatesUsed = !locationFalsePositiveRates.isEmpty();

        double estimatedFalsePositives = estimateFalsePositives(tagFalsePositiveRates, locationFalsePositiveRates);

        return new FalsePositiveSummary(
            ongoingOfflineEvents,
            historicalTags,
            tagFalsePositiveRates,
            locationFalsePositiveRates,
            estimatedFalsePositives,
            locationRatesUsed
        );
    }

    /**
     * Normalizes a tag value by replacing null or blank values with "Unknown".
     *
     * @param tag raw tag string
     * @return cleaned tag value
     */
    private String normalizeTag(String tag) {
        return tag == null || tag.trim().isEmpty() ? "Unknown" : tag.trim();
    }

    /**
     * Applies predefined base false positive rates to each tag based on historical event counts.
     *
     * @param historical map of tag → event count
     * @param baseRates  fallback tag → false positive rate
     * @return map of tag → applied false positive rate
     */
    private Map<String, Double> computeFalsePositiveRates(Map<String, Integer> historical, Map<String, Double> baseRates) {
        Map<String, Double> rates = new HashMap<>();
        for (var entry : historical.entrySet()) {
            String tag = normalizeTag(entry.getKey());
            rates.put(tag, baseRates.getOrDefault(tag, 0.1));
        }
        rates.putIfAbsent("Unknown", baseRates.get("Unknown"));
        return rates;
    }

    /**
     * Determines whether the system has enough historical data to learn accurate false positive rates.
     *
     * @return true if ≥ 50 events have confirmed false positive values
     */
    private boolean hasSufficientHistoricalData() {
        Integer count = jdbc.queryForObject("""
            SELECT COUNT(*) FROM OfflineEvents
            WHERE confirmed_false_positive IS NOT NULL
        """, Integer.class);
        return count != null && count >= 50;
    }

    /**
     * Computes false positive rates per tag using confirmed historical events.
     * Only tags with at least 5 confirmed entries are considered.
     *
     * @return map of tag → learned false positive rate
     */
    private Map<String, Double> computeLearnedTagFalsePositiveRates() {
        Map<String, Double> result = new HashMap<>();
        String sql = """
            SELECT tag,
                   COUNT(*) AS total,
                   SUM(CASE WHEN confirmed_false_positive = TRUE THEN 1 ELSE 0 END) AS false_count
            FROM OfflineEvents
            WHERE tag IS NOT NULL AND confirmed_false_positive IS NOT NULL
            GROUP BY tag
        """;
        jdbc.query(sql, rs -> {
            while (rs.next()) {
                String tag = normalizeTag(rs.getString("tag"));
                int total = rs.getInt("total");
                int falseCount = rs.getInt("false_count");
                if (total >= 5) {
                    double rate = falseCount / (double) total;
                    result.put(tag, rate);
                }
            }
            return null;
        });
        return result;
    }

    /**
     * Computes false positive rates per location using confirmed historical events.
     * Only locations with at least 5 confirmed entries are considered.
     *
     * @return map of location → learned false positive rate
     */
    private Map<String, Double> computeLearnedLocationFalsePositiveRates() {
        Map<String, Double> result = new HashMap<>();
        String sql = """
            SELECT location,
                   COUNT(*) AS total,
                   SUM(CASE WHEN confirmed_false_positive = TRUE THEN 1 ELSE 0 END) AS false_count
            FROM OfflineEvents
            WHERE location IS NOT NULL AND confirmed_false_positive IS NOT NULL
            GROUP BY location
        """;
        jdbc.query(sql, rs -> {
            while (rs.next()) {
                String location = rs.getString("location");
                int total = rs.getInt("total");
                int falseCount = rs.getInt("false_count");
                if (total >= 5) {
                    double rate = falseCount / (double) total;
                    result.put(location, rate);
                }
            }
            return null;
        });
        return result;
    }

    /**
     * Estimates how many currently ongoing offline events are likely false positives.
     * Combines tag and location probabilities using:
     * {@code 1 - (1 - tagProb) * (1 - locProb)}.
     *
     * @param tagRates      map of tag → false positive probability
     * @param locationRates map of location → false positive probability
     * @return estimated number of false positives among ongoing events
     */
    private double estimateFalsePositives(Map<String, Double> tagRates, Map<String, Double> locationRates) {
        String sql = """
            SELECT location, tag
            FROM OfflineEvents
            WHERE restored_at IS NULL
        """;
        return jdbc.query(sql, rs -> {
            double falsePositiveSum = 0.0;
            while (rs.next()) {
                String tag = normalizeTag(rs.getString("tag"));
                String location = rs.getString("location");

                double tagProb = tagRates.getOrDefault(tag, 0.1);
                double locProb = location != null ? locationRates.getOrDefault(location, 0.0) : 0.0;

                double combinedProb = locationRates.isEmpty()
                    ? tagProb
                    : 1.0 - ((1.0 - tagProb) * (1.0 - locProb));

                falsePositiveSum += (1.0 - combinedProb);
            }
            return falsePositiveSum;
        });
    }
}
