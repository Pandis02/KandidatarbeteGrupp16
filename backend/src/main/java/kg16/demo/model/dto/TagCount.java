package kg16.demo.model.dto;

/**
 * Represents the number of events associated with a specific tag.
 * Commonly used for generating tag-based statistics or visual summaries.
 *
 * @param tag    the label or category assigned to the event (e.g., "Power Outage", "Maintenance")
 * @param count  the number of events with this tag
 */
public record TagCount(String tag, int count) {}
