package kg16.demo.model.records;

/**
 * Represents a device identified as problematic based on event frequency and total downtime.
 * Typically used for rankings, reports, or monitoring dashboards to highlight unstable devices.
 *
 * @param macAddress       the MAC address of the device
 * @param eventCount       the total number of offline events recorded for this device
 * @param downtimeSeconds  the cumulative downtime duration for the device in seconds
 */
public record ProblematicDevice(String macAddress, long eventCount, long downtimeSeconds) {}
