package kg16.demo.web;

import kg16.demo.model.dto.ConfirmationUpdateDTO;
import kg16.demo.model.dto.LogDTO;
import kg16.demo.model.dto.TagUpdateDTO;
import kg16.demo.model.services.LogService;

import org.springframework.stereotype.Controller;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller responsible for displaying and interacting with the offline event logs.
 * Supports pagination, search, and inline updates for tags and false positive confirmations.
 */
@Controller
public class ViewLogs {

    private final LogService logService;

    public ViewLogs(LogService logService) {
        this.logService = logService;
    }

    /**
     * Renders the logs view with optional search and pagination.
     *
     * @param page   the page number to display (1-indexed)
     * @param rows   number of rows per page
     * @param search optional search string to filter by MAC address or hostname
     * @param model  the Thymeleaf model to populate for the view
     * @return the name of the view template (logs.html)
     */
    @GetMapping("/logs")
    public String onViewLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int rows,
            @RequestParam(defaultValue = "") String search,
            Model model) {

        search = (search != null) ? search.trim() : "";

        List<LogDTO> logs = logService.findOfflineEvents(null, null); // Load all logs for now

        // Apply search filter on MAC address or hostname
        if (!search.isEmpty()) {
            String lcs = search.toLowerCase();
            logs = logs.stream().filter(
                    log -> log.getMacAddress().toLowerCase().contains(lcs) ||
                           log.getHostname().toLowerCase().contains(lcs)
            ).toList();
        }

        int totalPages = Math.ceilDiv(logs.size(), rows);
        page = Math.max(1, Math.min(page, totalPages));
        int startIndex = (page - 1) * rows;
        int endIndex = Math.min(startIndex + rows, logs.size());
        List<LogDTO> paginatedLogs = logs.subList(startIndex, endIndex);

        model.addAttribute("logs", paginatedLogs);
        model.addAttribute("page", page);
        model.addAttribute("pages", totalPages);
        model.addAttribute("rows", rows);
        model.addAttribute("search", search);

        return "logs";
    }

    /**
     * Updates the tag associated with a specific offline event.
     *
     * @param request the tag update request body containing event ID and new tag
     * @return HTTP 200 OK response
     */
    @PostMapping("/logs/update-tag")
    public ResponseEntity<Void> updateTag(@RequestBody TagUpdateDTO request) {
        logService.updateTag(request.getEventId(), request.getTag());
        return ResponseEntity.ok().build();
    }

    /**
     * Updates the confirmed false positive status of a specific offline event.
     *
     * @param request the confirmation update request body with event ID and boolean flag
     * @return HTTP 200 OK response
     */
    @PostMapping("/logs/update-confirmation")
    public ResponseEntity<Void> updateConfirmation(@RequestBody ConfirmationUpdateDTO request) {
        logService.updateConfirmedFalsePositive(request.getEventId(), request.getConfirmed());
        return ResponseEntity.ok().build();
    }
}
