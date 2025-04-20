package kg16.demo.web;

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

        Status status = new Status("All good!", "green");
        if (totalOnline >= totalDevices * 0.75 && totalOnline < totalDevices) {
            status = new Status("Operational!", "yellow");
        } else if (totalOnline < totalDevices * 0.75) {
            status = new Status("Attention Needed!", "red");
        }

        model.addAttribute("alertTimings", ds.getAlertsTimings());
        model.addAttribute("dayAlerts", ds.get7DayAlerts());

        model.addAttribute("totalDevices", totalDevices);
        model.addAttribute("totalOnline", totalOnline);
        model.addAttribute("totalOffline", totalOffline);

        model.addAttribute("buildings", buildings);
        model.addAttribute("status", status);

        return "dashboard";
    }

    public record Status(String text, String color) {
    }
}
