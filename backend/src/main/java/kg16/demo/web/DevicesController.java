package kg16.demo.web;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import kg16.demo.model.services.DeviceService;

@Controller
public class DevicesController {
    private final DeviceService ds;

    public DevicesController(DeviceService ds) {
        this.ds = ds;
    }

    @GetMapping("/devices")
    public String onDevices(Model model) {
        model.addAttribute("devices", ds.getAllDevices());
        return "devices";
    }

    @GetMapping("/device-update-state")
    public ResponseEntity<Void> onEnabledTracking(@RequestParam String macAddress, @RequestParam Boolean enabled) {
        ds.setEnabled(macAddress, enabled);
        return ResponseEntity.ok().build();
    }
}