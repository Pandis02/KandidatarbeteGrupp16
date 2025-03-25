package kg16.demo.web;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import kg16.demo.model.services.DeviceService;

@Controller
public class ViewDevices {
    private final DeviceService ds;

    public ViewDevices(DeviceService ds) {
        this.ds = ds;
    }

    @GetMapping("/devices")
    public String onDevices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int rows,
            Model model) {
        var allDevices = ds.getAllDevices();
        int totalPages = Math.ceilDiv(allDevices.size(), rows);

        // Ensure page is within valid range
        page = Math.max(1, Math.min(page, totalPages));

        // Calculate start and end indices for pagination
        int startIndex = (page - 1) * rows;
        int endIndex = Math.min(startIndex + rows, allDevices.size());

        var paginatedDevices = allDevices.subList(startIndex, endIndex);

        model.addAttribute("devices", paginatedDevices);
        model.addAttribute("page", page);
        model.addAttribute("pages", totalPages);
        model.addAttribute("rows", rows);

        return "devices";
    }

    @GetMapping("/device-update-state")
    public ResponseEntity<Void> onEnabledTracking(@RequestParam String macAddress, @RequestParam Boolean enabled) {
        ds.setEnabled(macAddress, enabled);
        return ResponseEntity.ok().build();
    }
}