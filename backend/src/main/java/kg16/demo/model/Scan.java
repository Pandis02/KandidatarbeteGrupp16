package kg16.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "Scans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
}
