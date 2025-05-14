package kg16.demo.model.dto;

/**
 * Represents a device that has received the most alert notifications.
 * Typically used to identify noisy or problematic devices based on alert frequency.
 *
 * @param macAddress         the MAC address of the device
 * @param notificationCount  the total number of notifications sent for this device
 */
public record MostNotifiedDevice(String macAddress, int notificationCount) {}
