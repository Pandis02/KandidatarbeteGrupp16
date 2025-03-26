package kg16.demo.web;

import kg16.demo.model.services.RecipientService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;

/**
 * Controller for managing recipients.
 */
@Controller
public class RecipientController {
    private final RecipientService rs;

    /**
     * Constructs a new RecipientController with the given RecipientService.
     *
     * @param recipientService the recipient service
     */
    public RecipientController(RecipientService recipientService) {
        this.rs = recipientService;
    }

    /**
     * Gets the main page
     */
    @GetMapping("/recipients")
    public String getRecipients(Model model) {
        model.addAttribute("recipients", rs.getAllRecipients());
        return "recipients";
    }

    /**
     * Adds a new recipient.
     *
     * @param type  the type of the recipient (e.g., email, SMS, push token)
     * @param value the value of the recipient (e.g., email address, phone number, push token)
     * @return the added recipient
     */
    @GetMapping("/add-recipient")
    public String addRecipient(@RequestParam String type, @RequestParam String value, RedirectAttributes rda) {
        try {
            rs.addRecipient(type, value);
        } catch (Exception e) {
            rda.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/recipients";
    }

    /**
     * Deletes a recipient by ID.
     *
     * @param recipientId the ID of the recipient to delete
     * @return a response indicating the result of the deletion
     */
    @GetMapping("/remove-recipient")
    public String deleteRecipient(@RequestParam Long recipientId) {
        rs.deleteRecipient(recipientId);
        return "redirect:/recipients";
    }

    /**
     * Assigns a recipient to a role.
     *
     * @param recipientId the ID of the recipient
     * @param roleName    the name of the role to assign
     * @return a response indicating the result of the assignment
     */
    @GetMapping("/assign-role")
    public String assignRecipientToRole(@RequestParam Long recipientId, @RequestParam String roleName,
            RedirectAttributes rda) {
        try {
            rs.assignRecipientToRole(recipientId, roleName);
        } catch (Exception e) {
            rda.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/recipients";
    }

    /**
     * Removes a recipient from a role.
     *
     * @param recipientId the ID of the recipient
     * @param roleName    the name of the role to remove
     * @return a response indicating the result of the removal
     */
    @GetMapping("/remove-role")
    public String removeRecipientFromRole(@RequestParam Long recipientId, @RequestParam String roleName,
            RedirectAttributes rda) {
        try {
            rs.removeRecipientFromRole(recipientId, roleName);
        } catch (Exception e) {
            rda.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/recipients";
    }
}