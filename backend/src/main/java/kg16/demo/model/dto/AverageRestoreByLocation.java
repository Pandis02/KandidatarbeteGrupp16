package kg16.demo.model.dto;

/**
 * Data transfer object representing the average restore time (in seconds)
 * for a specific location, based on historical OfflineEvent data.
 *
 * Used to display and format average recovery durations per site.
 *
 * @param location        the name or identifier of the location
 * @param averageSeconds  the average duration in seconds before devices are restored
 */
public record AverageRestoreByLocation(String location, double averageSeconds) {

    /**
     * Converts the average duration into a human-readable format (e.g. "2h 15m").
     * - Omits hours if zero (e.g. "45m")
     *
     * @return a formatted string representing the duration in hours and minutes
     */
    public String formatted() {
        long seconds = (long) averageSeconds;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;

        return hours > 0
            ? String.format("%dh %dm", hours, minutes)
            : String.format("%dm", minutes);
    }
}
