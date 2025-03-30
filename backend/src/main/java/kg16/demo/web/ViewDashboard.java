package kg16.demo.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


import kg16.demo.model.services.DashboardService;

@Controller
public class ViewDashboard {
    private final DashboardService ds;

    public ViewDashboard(DashboardService ds){
        this.ds = ds;
    }

    @GetMapping("/dashboard")
    public String onDashboard(Model model){

        var counts = ds.getDeviceStatusCount();
        var recentOfflineDevices = ds.getTopRecentOfflineDevices(3);

        model.addAttribute("devices", recentOfflineDevices);
        model.addAttribute("status", counts);

        return "dashboard";
    }
}
