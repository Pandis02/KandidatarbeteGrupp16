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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int rows,
            @RequestParam(required = false) String search,
            Model model) {
        
        // Fetch logs based on date range
        List<LogDTO> logs = logService.findOfflineEvents(startDate, endDate);
    
        // Apply search filtering if a search term is provided
        if (search != null && !search.trim().isEmpty()) {
            String lowerCaseSearch = search.toLowerCase();
            logs = logs.stream()
                    .filter(log -> log.getMacAddress().toLowerCase().contains(lowerCaseSearch) ||
                                   log.getHostname().toLowerCase().contains(lowerCaseSearch) ||
                                   log.getNotificationType().toLowerCase().contains(lowerCaseSearch))
                    .toList();
        }
    
        // Calculate total pages
        int totalPages = Math.ceilDiv(logs.size(), rows);
    
        // Ensure page is within valid range
        page = Math.max(1, Math.min(page, totalPages));
    
        // Calculate start and end indices for pagination
        int startIndex = (page - 1) * rows;
        int endIndex = Math.min(startIndex + rows, logs.size());
    
        // Get paginated logs
        List<LogDTO> paginatedLogs = logs.subList(startIndex, endIndex);
    
        // Add attributes to the model
        model.addAttribute("logs", paginatedLogs);
        model.addAttribute("page", page);
        model.addAttribute("pages", totalPages);
        model.addAttribute("rows", rows);
        model.addAttribute("search", search);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
    
        return "logs";
    }
}