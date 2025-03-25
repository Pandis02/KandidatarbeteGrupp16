package kg16.demo.web;

import kg16.demo.model.dto.LogDTO;
import kg16.demo.model.services.LogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
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
    public String onViewLogs(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                             Model model) {
        List<LogDTO> logs = logService.findOfflineEvents(startDate, endDate);
        model.addAttribute("logs", logs);
        return "logs";
    }
}