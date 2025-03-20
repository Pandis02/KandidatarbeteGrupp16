package kg16.demo.web;

import kg16.demo.model.dto.Recipient;
import kg16.demo.model.services.RecipientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing recipients.
 */
@RestController
@RequestMapping("/api/recipients")
public class RecipientController {
    private final RecipientService recipientService;

    /**
     * Constructs a new RecipientController with the given RecipientService.
     *
     * @param recipientService the recipient service
     */
    public RecipientController(RecipientService recipientService) {
        this.recipientService = recipientService;
    }

    /**
     * Adds a new recipient.
     *
     * @param type  the type of the recipient (e.g., email, SMS, push token)
     * @param value the value of the recipient (e.g., email address, phone number, push token)
     * @return the added recipient
     */
    @PostMapping("/add")
    public ResponseEntity<Recipient> addRecipient(
        @RequestParam String type,
        @RequestParam String value
    ) {
        return ResponseEntity.ok(recipientService.addRecipient(type, value));
    }

    /**
     * Retrieves all recipients.
     *
     * @return a list of all recipients
     */
    @GetMapping("/all")
    public ResponseEntity<List<Recipient>> getAllRecipients() {
        return ResponseEntity.ok(recipientService.getAllRecipients());
    }

    /**
     * Deletes a recipient by ID.
     *
     * @param recipientId the ID of the recipient to delete
     * @return a response indicating the result of the deletion
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteRecipient(@RequestParam Long recipientId) {
        recipientService.deleteRecipient(recipientId);
        return ResponseEntity.ok("Recipient deleted.");
    }

    /**
     * Assigns a recipient to a role.
     *
     * @param recipientId the ID of the recipient
     * @param roleName    the name of the role to assign
     * @return a response indicating the result of the assignment
     */
    @PostMapping("/assign-role")
    public ResponseEntity<String> assignRecipientToRole(
        @RequestParam Long recipientId,
        @RequestParam String roleName
    ) {
        recipientService.assignRecipientToRole(recipientId, roleName);
        return ResponseEntity.ok("Recipient assigned to role.");
    }

    /**
     * Removes a recipient from a role.
     *
     * @param recipientId the ID of the recipient
     * @param roleName    the name of the role to remove
     * @return a response indicating the result of the removal
     */
    @PostMapping("/remove-role")
    public ResponseEntity<String> removeRecipientFromRole(
        @RequestParam Long recipientId,
        @RequestParam String roleName
    ) {
        recipientService.removeRecipientFromRole(recipientId, roleName);
        return ResponseEntity.ok("Recipient removed from role.");
    }
}