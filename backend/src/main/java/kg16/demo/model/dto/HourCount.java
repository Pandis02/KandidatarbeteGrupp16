package kg16.demo.model.dto;

/**
 * Represents the number of events that occurred during a specific hour of the day.
 * Typically used for hourly distribution charts or heatmaps.
 *
 * @param hour   the hour of the day (0–23)
 * @param count  the number of events that occurred during that hour
 */
public record HourCount(int hour, int count) {}
