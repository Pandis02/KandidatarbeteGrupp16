package kg16.demo.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import kg16.demo.model.services.DashboardService;

@Controller
public class ViewDashboard {
    private final DashboardService ds;

    public ViewDashboard(DashboardService ds) {
        this.ds = ds;
    }

    @GetMapping("/dashboard")
    public String onDashboard(Model model) {

        var counts = ds.getCounts();
        var buildings = ds.getBuildingsInfo();

        int totalDevices = buildings.stream()
                .mapToInt(DashboardService.Building::totalDevices)
                .sum();

        int totalOnline = buildings.stream()
                .mapToInt(DashboardService.Building::onlineDevices)
                .sum();

        int totalOffline = buildings.stream()
                .mapToInt(DashboardService.Building::offlineDevices)
                .sum();

        model.addAttribute("totalDevices", totalDevices);
        model.addAttribute("totalOnline", totalOnline);
        model.addAttribute("totalOffline", totalOffline);

        model.addAttribute("buildings", buildings);

        return "dashboard";
    }
}
