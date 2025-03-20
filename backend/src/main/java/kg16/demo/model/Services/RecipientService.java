package kg16.demo.model.services;

import kg16.demo.model.dto.Recipient;
import kg16.demo.model.dto.Role;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing recipients.
 */
@Service
public class RecipientService {
    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructs a new RecipientService with the given JdbcTemplate.
     *
     * @param jdbcTemplate the JdbcTemplate to use for database operations
     */
    public RecipientService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Adds a new recipient.
     *
     * @param type  the type of the recipient (e.g., email, SMS, push token)
     * @param value the value of the recipient (e.g., email address, phone number, push token)
     * @return the added recipient
     * @throws IllegalArgumentException if the recipient already exists
     */
    public Recipient addRecipient(String type, String value) {
        String checkQuery = "SELECT COUNT(*) FROM Recipients WHERE recipient_value = ?";
        int count = jdbcTemplate.queryForObject(checkQuery, Integer.class, value);

        if (count > 0) {
            throw new IllegalArgumentException("Recipient already exists.");
        }

        String insertQuery = "INSERT INTO Recipients (recipient_type, recipient_value) VALUES (?, ?)";
        jdbcTemplate.update(insertQuery, type, value);

        return new Recipient(null, type, value, new ArrayList<>());
    }

    /**
     * Retrieves all recipients.
     *
     * @return a list of all recipients
     */
    public List<Recipient> getAllRecipients() {
        String sql = "SELECT * FROM Recipients";

        List<Recipient> recipients = jdbcTemplate.query(sql, (rs, rowNum) -> 
            new Recipient(
                rs.getLong("recipient_id"),
                rs.getString("recipient_type"),
                rs.getString("recipient_value"),
                new ArrayList<>()
            )
        );

        for (Recipient recipient : recipients) {
            recipient.setRoles(getRolesForRecipient(recipient.getRecipientId()));
        }

        return recipients;
    }

    /**
     * Retrieves the roles for a given recipient.
     *
     * @param recipientId the ID of the recipient
     * @return a list of roles for the recipient
     */
    public List<Role> getRolesForRecipient(Long recipientId) {
        String sql = "SELECT r.role_id, r.role_name FROM Roles r " +
                     "JOIN RecipientRoles rr ON r.role_id = rr.role_id " +
                     "WHERE rr.recipient_id = ?";

        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> 
                new Role(
                    rs.getLong("role_id"),
                    rs.getString("role_name")
                ), recipientId
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>(); 
        }
    }

    /**
     * Deletes a recipient by ID.
     *
     * @param recipientId the ID of the recipient to delete
     */
    public void deleteRecipient(Long recipientId) {
        String deleteQuery = "DELETE FROM Recipients WHERE recipient_id = ?";
        jdbcTemplate.update(deleteQuery, recipientId);
    }

    /**
     * Assigns a recipient to a role.
     *
     * @param recipientId the ID of the recipient
     * @param roleName    the name of the role to assign
     * @throws IllegalArgumentException if the role is not found or the recipient already has the role
     */
    public void assignRecipientToRole(Long recipientId, String roleName) {
        String roleQuery = "SELECT role_id FROM Roles WHERE role_name = ?";
        Long roleId;

        try {
            roleId = jdbcTemplate.queryForObject(roleQuery, Long.class, roleName);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("Role not found.");
        }

        String checkExistingQuery = "SELECT COUNT(*) FROM RecipientRoles WHERE recipient_id = ? AND role_id = ?";
        int count = jdbcTemplate.queryForObject(checkExistingQuery, Integer.class, recipientId, roleId);

        if (count > 0) {
            throw new IllegalArgumentException("Recipient already has this role.");
        }

        String insertQuery = "INSERT INTO RecipientRoles (recipient_id, role_id) VALUES (?, ?)";
        jdbcTemplate.update(insertQuery, recipientId, roleId);
    }

    /**
     * Removes a recipient from a role.
     *
     * @param recipientId the ID of the recipient
     * @param roleName    the name of the role to remove
     * @throws IllegalArgumentException if the role is not found or the role assignment is not found
     */
    public void removeRecipientFromRole(Long recipientId, String roleName) {
        String roleQuery = "SELECT role_id FROM Roles WHERE role_name = ?";
        Long roleId;

        try {
            roleId = jdbcTemplate.queryForObject(roleQuery, Long.class, roleName);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("Role not found.");
        }

        String deleteQuery = "DELETE FROM RecipientRoles WHERE recipient_id = ? AND role_id = ?";
        int affectedRows = jdbcTemplate.update(deleteQuery, recipientId, roleId);

        if (affectedRows == 0) {
            throw new IllegalArgumentException("Role assignment not found.");
        }
    }
}