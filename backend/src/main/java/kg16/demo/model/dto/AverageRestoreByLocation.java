package kg16.demo.model.dto;

public record AverageRestoreByLocation(String location, double averageSeconds) {

    public String formatted() {
        long seconds = (long) averageSeconds;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;

        return hours > 0
            ? String.format("%dh %dm", hours, minutes)
            : String.format("%dm", minutes);
    }
}
