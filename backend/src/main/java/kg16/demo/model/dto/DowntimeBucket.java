package kg16.demo.model.dto;

/**
 * Represents a bucketed count of events based on their downtime duration.
 * Typically used in histograms to show distribution of offline event durations.
 *
 * @param rangeLabel  a human-readable label for the time range (e.g., "0–5 min", "1–2h")
 * @param count       the number of events that fall into this downtime range
 */
public record DowntimeBucket(String rangeLabel, int count) {}
