package kg16.demo.model.records;

import java.sql.Timestamp;

public record ScanRecord(String macAddress, String ipAddress, String hostname, Timestamp lastSeen) {
}
