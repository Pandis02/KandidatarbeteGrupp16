package kg16.demo.web;

import kg16.demo.model.dto.LogDTO;
import kg16.demo.model.services.LogService;
import org.springframework.stereotype.Controller;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class ViewLogs {
    private final LogService logService;

    public ViewLogs(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("/logs")
    public String onViewLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int rows,
            @RequestParam(defaultValue = "") String search,
            Model model) {

        // Ensure search is not null
        search = (search != null) ? search.trim() : "";

        // Fetch logs based on date range
        List<LogDTO> logs = logService.findOfflineEvents(null, null);

        // Apply search filtering if a search term is provided
        if (search != null && !search.trim().isEmpty()) {
            String lcs = search.toLowerCase();
            logs = logs.stream().filter(
                    log -> log.getMacAddress().toLowerCase().contains(lcs) ||
                            log.getHostname().toLowerCase().contains(lcs) ||
                            (log.getNotificationType() != null
                                    && log.getNotificationType().toLowerCase().contains(lcs)))
                    .toList();
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
}