package kg16.demo.model.dto;

public record TopDevice(String macAddress, int eventCount, long downtimeSeconds) {
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