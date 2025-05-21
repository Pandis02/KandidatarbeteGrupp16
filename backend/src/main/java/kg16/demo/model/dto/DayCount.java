package kg16.demo.model.dto;

/**
 * Represents the number of events that occurred on a specific date.
 * Typically used for visualizations such as line charts or bar graphs
 * showing daily trends in event frequency.
 *
 * @param date   the date in string format (e.g., "2025-05-14")
 * @param count  the number of events recorded on that date
 */
public record DayCount(String date, int count) {}
