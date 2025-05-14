package kg16.demo.model.dto;

/**
 * Represents the number of events that occurred on a specific weekday.
 * Commonly used for generating weekday-based bar charts or usage patterns.
 *
 * @param weekday the name of the weekday (e.g., "Monday", "Tuesday")
 * @param count   the number of events that occurred on that weekday
 */
public record WeekdayCount(String weekday, int count) {}
