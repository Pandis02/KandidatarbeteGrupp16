package kg16.demo.model.dto;

import lombok.*;
import java.util.List;

/**
 * Represents a recipient of notifications within the system.
 * A recipient has a type (e.g., "email", "sms", "webhook"), a value (e.g., the address or identifier),
 * and a list of roles that define its alert permissions or visibility scope.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Recipient {

    /** Unique ID of the recipient (typically maps to a database row). */
    private Long recipientId;

    /** The type of recipient (e.g., "Email", "SMS", "Slack", "Webhook"). */
    private String recipientType;

    /** The value for the recipient, such as an email address or phone number. */
    private String recipientValue;

    /** Roles associated with the recipient, used for filtering or permission control. */
    private List<Role> roles;

    /**
     * Checks whether the recipient has a specific role assigned.
     *
     * @param roleName the name of the role to check
     * @return true if the recipient has the role, false otherwise
     */
    public boolean hasRole(String roleName) {
        for (Role role : roles) {
            if (role.getRoleName().equals(roleName)) {
                return true;
            }
        }
        return false;
    }
}
