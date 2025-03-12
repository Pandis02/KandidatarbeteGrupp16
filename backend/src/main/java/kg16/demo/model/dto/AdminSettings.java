package kg16.demo.model.dto;

import java.sql.Timestamp;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminSettings {
    private int alertThresholdMinutes;
    private int checkinIntervalSeconds;
    private Timestamp lastUpdated;
}
