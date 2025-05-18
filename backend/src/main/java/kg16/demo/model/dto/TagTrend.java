package kg16.demo.model.dto;

/**
 * Represents the number of events with a specific tag on a specific date.
 * Useful for trend analysis and visualizations (e.g., line charts showing tag frequency over time).
 *
 * @param date   the date of the events (e.g., "2025-05-14")
 * @param tag    the tag assigned to the events (e.g., "Power Outage", "Network Issue")
 * @param count  the number of events on that date with the given tag
 */
public record TagTrend(String date, String tag, int count) {}
