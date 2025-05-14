package kg16.demo.model.dto;

/**
 * Represents a device with the highest number of offline events and its total downtime.
 * Used to identify the most problematic or frequently affected devices in the system.
 *
 * @param macAddress       the MAC address of the device
 * @param eventCount       the number of offline events associated with this device
 * @param downtimeSeconds  the total downtime for this device in seconds
 */
public record TopDevice(String macAddress, int eventCount, long downtimeSeconds) {

    /**
     * Formats the total downtime into a human-readable format (e.g., "2d 3h 15m").
     * Omits zero-value segments unless needed for context.
     *
     * @return a formatted downtime string
     */
    public String formattedDowntime() {
        long seconds = downtimeSeconds;
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0 || days > 0) sb.append(hours).append("h ");
        sb.append(minutes).append("m");

        return sb.toString().trim();
    }
}
