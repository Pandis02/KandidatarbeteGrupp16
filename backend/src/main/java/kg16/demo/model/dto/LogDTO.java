package kg16.demo.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogDTO {
    private String hostname;
    private String macAddress;
    private String offlineSince;
    private String restoredAt;
    private boolean alertSent;
    private String notificationMessage;
    private String notificationTimestamp;
    private String notificationRecipient;
    private String notificationType;
}