package kg16.demo.model.dto;

public class LogDTO {
    private Integer deviceId;
    private String hostname;
    private String ipAddress;
    private String macAddress;
    private Integer status;
    private String lastSeen;
    private String offlineSince;
    private String restoredAt;

    public LogDTO(Integer deviceId, String hostname, String ipAddress, String macAddress,
                  Integer status, String lastSeen, String offlineSince, String restoredAt) {
        this.deviceId = deviceId;
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.status = status;
        this.lastSeen = lastSeen;
        this.offlineSince = offlineSince;
        this.restoredAt = restoredAt;
    }

    // Getters and setters
    public Integer getDeviceId() { return deviceId; }
    public void setDeviceId(Integer deviceId) { this.deviceId = deviceId; }

    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getMacAddress() { return macAddress; }
    public void setMacAddress(String macAddress) { this.macAddress = macAddress; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getLastSeen() { return lastSeen; }
    public void setLastSeen(String lastSeen) { this.lastSeen = lastSeen; }

    public String getOfflineSince() { return offlineSince; }
    public void setOfflineSince(String offlineSince) { this.offlineSince = offlineSince; }

    public String getRestoredAt() { return restoredAt; }
    public void setRestoredAt(String restoredAt) { this.restoredAt = restoredAt; }
}
