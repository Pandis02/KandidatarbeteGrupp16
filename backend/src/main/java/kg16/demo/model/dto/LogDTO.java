package kg16.demo.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogDTO {
    private String hostname;
    private String ipAddress;
    private String macAddress;
    private String lastSeen;
    private String offlineSince;
    private String restoredAt;
}
