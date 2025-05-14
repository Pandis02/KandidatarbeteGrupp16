package kg16.demo.model.dto;

import lombok.*;

/**
 * DTO used to update the confirmation status of a specific offline event.
 * Typically sent from the frontend when a user marks or unmarks an event as a false positive.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmationUpdateDTO {

    /** The ID of the offline event to update. */
    private Long eventId;

    /** Whether the event is confirmed as a false positive (true), not a false positive (false), or unmarked (null). */
    private Boolean confirmed;
}
