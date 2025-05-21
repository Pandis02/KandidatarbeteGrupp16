package kg16.demo.model.dto;

import lombok.*;

/**
 * Represents a system role that can be assigned to recipients or users.
 * Roles are typically used to control access to notifications or define alert visibility.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    /** Unique identifier for the role (typically from the database). */
    private Long roleId;

    /** The name of the role (e.g., "Admin", "Security", "Observer"). */
    private String roleName;
}
