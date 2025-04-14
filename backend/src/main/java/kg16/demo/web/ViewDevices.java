package kg16.demo.web;

import java.util.Comparator;

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
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "name") String sortBy,
            Model model) {

        var allDevices = ds.getAllRegisteredDevices();

        // Apply search filtering if a search term is provided
        if (search != null && !search.trim().isEmpty()) {
            String lcs = search.toLowerCase();
            allDevices = allDevices.stream().filter(
                    device -> device.macAddress().toLowerCase().contains(lcs) ||
                            device.name().toLowerCase().contains(lcs))
                    .toList();
        }

        // Apply sorting based on the sortBy parameter
        allDevices = switch (sortBy) {
            case "name" -> allDevices.stream()
                    .sorted(Comparator.comparing(DeviceService.Instance::name,
                            String.CASE_INSENSITIVE_ORDER))
                    .toList();
            case "location" -> allDevices.stream()
                    .sorted(Comparator
                            .comparing(DeviceService.Instance::building,
                                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                            .thenComparing(DeviceService.Instance::room,
                                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                    .toList();
            case "tracking" -> allDevices.stream()
                    .sorted(Comparator.comparing(DeviceService.Instance::enabled))
                    .toList();
            case "status" -> allDevices.stream()
                    .sorted(Comparator.comparing(DeviceService.Instance::status))
                    .toList();
            case "lastCheckin" -> allDevices.stream()
                    .sorted(Comparator.comparing(DeviceService.Instance::lastCheckin).reversed())
                    .toList();
            default -> allDevices.stream()
                    .sorted(Comparator.comparing(DeviceService.Instance::name,
                            String.CASE_INSENSITIVE_ORDER))
                    .toList();
        };

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
        model.addAttribute("search", search);
        model.addAttribute("sortBy", sortBy);

        return "devices";
    }

    @GetMapping("/devices-update-state")
    public ResponseEntity<Void> onEnabledTracking(@RequestParam String macAddress, @RequestParam Boolean enabled) {
        ds.setEnabled(macAddress, enabled);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/devices-unregister")
    public String onUnRegister(@RequestParam String macAddress) {
        ds.delete(macAddress);
        return "redirect:/devices";
    }
}