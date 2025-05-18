package kg16.demo.model.dto;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Summary of estimated false positives among currently ongoing offline events.
 * Includes calculated probabilities per tag and location (when applicable),
 * along with formatted values for UI display.
 *
 * This DTO is produced by {@link kg16.demo.model.services.OfflineEventStatsService}.
 *
 * @param ongoingOfflineEvents         number of currently ongoing offline events
 * @param historicalTags               total occurrences of each tag in historical data
 * @param tagFalsePositiveRates        estimated false positive rates for each tag
 * @param locationFalsePositiveRates   estimated false positive rates for each location (if available)
 * @param estimatedFalsePositives      number of currently ongoing events estimated to be false positives
 * @param locationRatesUsed            whether location rates were applied in the estimation
 */
public record FalsePositiveSummary(
    int ongoingOfflineEvents,
    Map<String, Integer> historicalTags,
    Map<String, Double> tagFalsePositiveRates,
    Map<String, Double> locationFalsePositiveRates,
    double estimatedFalsePositives,
    boolean locationRatesUsed
) {

    /**
     * Calculates the false positive rate among ongoing offline events.
     *
     * @return a decimal value between 0.0 and 1.0
     */
    public double falsePositiveRate() {
        return ongoingOfflineEvents == 0 ? 0 : estimatedFalsePositives / ongoingOfflineEvents;
    }

    /**
     * Formats the false positive rate as a percentage string.
     *
     * @return formatted false positive rate (e.g., "43.2%")
     */
    public String formattedFalsePositiveRate() {
        return String.format("%.1f%%", falsePositiveRate() * 100);
    }

    /**
     * Formats the estimated false positives as a rounded string with a tilde prefix.
     *
     * @return formatted approximation (e.g., "~6.0" or "~6.5")
     */
    public String formattedEstimatedFalsePositives() {
        return estimatedFalsePositives % 1 == 0
            ? String.format("~%.0f", estimatedFalsePositives)
            : String.format("~%.1f", estimatedFalsePositives);
    }

    /**
     * Returns the top 5 tags most associated with false positives.
     * Sorted by highest false positive rate, descending.
     *
     * @return map of tag → false positive rate
     */
    public Map<String, Double> topFalsePositiveTags() {
        if (tagFalsePositiveRates == null) return Map.of();
        return tagFalsePositiveRates.entrySet().stream()
            .filter(e -> e.getValue() > 0)
            .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
            .limit(5)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }

    /**
     * Returns the top 5 locations most associated with false positives.
     * Only computed if {@code locationRatesUsed} is true.
     *
     * @return map of location → false positive rate
     */
    public Map<String, Double> topFalsePositiveLocations() {
        if (locationFalsePositiveRates == null) return Map.of();
        return locationFalsePositiveRates.entrySet().stream()
            .filter(e -> e.getValue() > 0)
            .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
            .limit(5)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }
}
