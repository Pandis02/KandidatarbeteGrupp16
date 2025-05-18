package kg16.demo.model.dto;

import lombok.*;

/**
 * Data Transfer Object representing a single offline event log entry.
 * Used to transfer log data between the backend and frontend, typically for UI tables or export features.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogDTO {

    /** Unique identifier for the offline event. */
    private Long eventId;

    /** Hostname of the affected device (if available). */
    private String hostname;

    /** MAC address of the affected device. */
    private String macAddress;

    /** Timestamp when the device went offline. */
    private String offlineSince;

    /** Timestamp when the device came back online (nullable if still offline). */
    private String restoredAt;

    /** Indicates whether an alert was sent for this event. */
    private boolean alertSent;

    /** Timestamp of the sent notification (if any). */
    private String notificationTimestamp;

    /** Recipients of the alert notification (e.g., email or SMS targets). */
    private String notificationRecipients;

    /** Location where the device was logged as offline. */
    private String location;

    /** Reason or category assigned to the event (e.g., "Power Outage"). */
    private String tag;

    /** Admin confirmation whether this event was a false positive (nullable if unreviewed). */
    private Boolean confirmedFalsePositive;
}
