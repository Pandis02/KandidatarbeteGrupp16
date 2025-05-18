package kg16.demo.model.dto;

/**
 * Represents the stability of a device, defined by its uptime percentage.
 * Used in system statistics to identify highly stable or unstable devices.
 *
 * @param macAddress         the MAC address of the device
 * @param uptimePercentage   the device's uptime as a percentage (0–100)
 */
public record DeviceStability(String macAddress, double uptimePercentage) {

    /**
     * Formats the uptime percentage as a string with two decimal places, followed by a percent sign.
     *
     * @return the formatted uptime string (e.g. "99.87%")
     */
    public String formatted() {
        return String.format("%.2f%%", uptimePercentage);
    }
}


