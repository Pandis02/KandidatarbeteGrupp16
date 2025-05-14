package kg16.demo.model.dto;

/**
 * Represents the number of events associated with a specific location.
 * Useful for generating location-based summaries or visualizations (e.g., pie charts or tables).
 *
 * @param location  the name or identifier of the location
 * @param count     the number of events recorded at that location
 */
public record LocationCount(String location, int count) {}
