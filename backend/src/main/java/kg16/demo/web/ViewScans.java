package kg16.demo.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kg16.demo.model.Database.Database;
import kg16.demo.dto.ScanDTO;
import java.util.List;

@Controller
@RequestMapping("/scans")
public class ViewScans {
    private final Database db;

    public ViewScans(Database db) {
        this.db = db;
    }

    @GetMapping
    public String onViewScans(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size,
                              Model model) {
        List<ScanDTO> scans = db.getPaginatedScans(page, size);
        model.addAttribute("scans", scans);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        return "scans";  // Thymeleaf template
    }
}

