package kg16.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "Scans") 
public class Scan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deviceId;

    @JsonProperty("hostname")
    @Column(name = "hostname", nullable = false)
    private String hostname;

    @JsonProperty("ipAddress") 
    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @JsonProperty("macAddress") 
    @Column(name = "mac_address", nullable = false, unique = true)
    private String macAddress;

    @JsonProperty("status") 
    @Column(name = "status", nullable = false)
    private Integer status;

    @JsonProperty("lastSeen") 
    @Column(name = "last_seen", nullable = false)
    private Timestamp lastSeen;

    // Empty constructor (required by JPA)
    public Scan() {}

    // Constructor
    public Scan(String hostname, String ipAddress, String macAddress, Integer status, Timestamp lastSeen) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.status = status;
        this.lastSeen = lastSeen;
    }

    // Getters and Setters
    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

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
