package kg16.demo.model.dto;

public record DeviceStability(String macAddress, double uptimePercentage) {
    public String formatted() {
        return String.format("%.2f%%", uptimePercentage);
    }
}

