package kg16.demo.model.dto;

import lombok.*;

/**
 * DTO used to update the tag of a specific offline event.
 * Typically sent from the frontend when an admin changes or adds a tag manually.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagUpdateDTO {

    /** ID of the offline event being updated. */
    private Long eventId;

    /** New or updated tag to assign to the event (e.g., "Maintenance", "Power Outage"). */
    private String tag;
}
