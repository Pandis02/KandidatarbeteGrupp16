package kg16.demo.model.records;

public record Checkin(String macAddress, String hostname, java.sql.Timestamp lastCheckin) {
}
