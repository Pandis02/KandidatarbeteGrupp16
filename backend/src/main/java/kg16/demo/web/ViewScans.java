package kg16.demo.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kg16.demo.model.Services.ScanService;
import kg16.demo.model.records.ScanRecord;
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
                          @RequestParam(defaultValue = "15") int size,
                          @RequestParam(defaultValue = "last_seen") String sortBy,
                          @RequestParam(defaultValue = "desc") String order,
                          Model model) {
    
    List<ScanRecord> scans = scanService.getScans(null, sortBy, order, page, size);
    model.addAttribute("scans", scans);
    model.addAttribute("currentPage", page);
    model.addAttribute("pageSize", size);
    model.addAttribute("sortBy", sortBy);
    model.addAttribute("order", order);

    return "scans";  // Thymeleaf template
}

}
