package kg16.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.sql.Timestamp;

public class ScanDTO {

    @NotBlank
    private String hostname;

    @NotBlank
    private String ipAddress;

    @NotBlank
    private String macAddress;

    @NotNull
    private Integer status;

    private Timestamp lastSeen;

    // Constructor
    public ScanDTO() {}

    public ScanDTO(String hostname, String ipAddress, String macAddress, Integer status, Timestamp lastSeen) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.status = status;
        this.lastSeen = lastSeen;
    }

    // Getters and Setters
    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Timestamp getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Timestamp lastSeen) {
        this.lastSeen = lastSeen;
    }
}
