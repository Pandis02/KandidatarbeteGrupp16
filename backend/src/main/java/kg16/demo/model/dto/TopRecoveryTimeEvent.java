package kg16.demo.model.dto;

/**
 * Represents an offline event with one of the longest recovery times.
 * Useful for identifying slow-recovering devices and visualizing extreme cases on dashboards.
 */
public class TopRecoveryTimeEvent {

    /** The MAC address of the device that experienced the event. */
    private final String macAddress;

    /** The recovery duration in seconds from offline to restored state. */
    private final long recoveryTimeSeconds;

    /** The timestamp (formatted as a string) when the device was restored. */
    private final String restoredAtFormatted;

    /**
     * Constructs a new TopRecoveryTimeEvent.
     *
     * @param macAddress           the device's MAC address
     * @param recoveryTimeSeconds  the time it took to recover in seconds
     * @param restoredAtFormatted  the human-readable timestamp of restoration
     */
    public TopRecoveryTimeEvent(String macAddress, long recoveryTimeSeconds, String restoredAtFormatted) {
        this.macAddress = macAddress;
        this.recoveryTimeSeconds = recoveryTimeSeconds;
        this.restoredAtFormatted = restoredAtFormatted;
    }

    /** @return the MAC address of the device */
    public String getMacAddress() {
        return macAddress;
    }

    /** @return the recovery duration in seconds */
    public long getRecoveryTimeSeconds() {
        return recoveryTimeSeconds;
    }

    /** @return the formatted restoration timestamp */
    public String getRestoredAtFormatted() {
        return restoredAtFormatted;
    }

    /**
     * Formats the recovery time into a human-readable string like "2h 15m" or "45m".
     *
     * @return a formatted duration string
     */
    public String getFormatted() {
        long seconds = recoveryTimeSeconds;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;

        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }
}
