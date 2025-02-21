package kg16.demo.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kg16.demo.model.Services.ScanService;
import kg16.demo.dto.ScanDTO;
import java.util.List;

@Controller
@RequestMapping("/scans")
public class ViewScans {
    private final ScanService scanService;

    public ViewScans(ScanService scanService) {
        this.scanService = scanService;
    }

    @GetMapping
    public String onViewScans(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size,
                              Model model) {
        if (page < 0) page = 0; // Ensure page is not negative
        
        List<ScanDTO> scans = scanService.getPaginatedScans(page, size);
        int totalScans = scanService.countScans(null);
        int totalPages = (int) Math.ceil((double) totalScans / size);

        model.addAttribute("scans", scans);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", totalPages);

        return "scans";  // Thymeleaf template
    }
}
