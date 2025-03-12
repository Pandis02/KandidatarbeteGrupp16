package kg16.demo.model.dto;

import java.sql.Timestamp;
import lombok.*;

/**
 * Data Transfer Object (DTO) for AdminSettings.
 * Represents the settings for the admin configuration.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminSettings {
    /**
     * The threshold in minutes for alerting.
     */
    private int alertThresholdMinutes;

    /**
     * The interval in seconds for check-ins.
     */
    private int checkinIntervalSeconds;

    /**
     * The timestamp of the last update.
     */
    private Timestamp lastUpdated;
}