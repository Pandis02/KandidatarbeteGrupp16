package kg16.demo.web;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kg16.demo.model.services.CheckinService;
import kg16.demo.model.services.DeviceService;
import kg16.demo.model.services.LocationsService;

@Controller
public class ViewDiscovery {
    private final CheckinService cs;
    private final DeviceService ds;
    private final LocationsService ls;

    public ViewDiscovery(CheckinService cs, DeviceService ds, LocationsService ls) {
        this.cs = cs;
        this.ds = ds;
        this.ls = ls;
    }

    @GetMapping("/discovery")
    public String onDiscovery(@RequestParam(required = false) String search, Model model) {
        var unregisteredCheckins = cs.getAllUnregisteredCheckins();
        var registeredDevicesCount = ds.getAllRegisteredDevices().size();
        var locations = ls.getAllLocations();

        // Extract unique buildings
        Set<String> uniqueBuildings = locations.stream()
                .map(LocationsService.Location::building)
                .collect(Collectors.toSet());

        // Apply search filtering if a search term is provided
        if (search != null && !search.trim().isEmpty()) {
            String lcs = search.toLowerCase();
            unregisteredCheckins = unregisteredCheckins.stream().filter(
                    checkin -> checkin.macAddress().toLowerCase().contains(lcs) ||
                            checkin.hostname().toLowerCase().contains(lcs))
                    .toList();
        }

        model.addAttribute("unregisteredCheckins", unregisteredCheckins);
        model.addAttribute("registeredDevicesCount", registeredDevicesCount);
        model.addAttribute("locations", locations);
        model.addAttribute("uniqueBuildings", uniqueBuildings);
        model.addAttribute("search", search);

        return "discovery";
    }

    @GetMapping("/discovery-register")
    public String onDiscoveryRegister(
            @RequestParam String macAddress,
            @RequestParam(defaultValue = "") String customName,
            @RequestParam int locationId,
            Model model) {
        cs.register(macAddress, customName, locationId);
        return "redirect:/discovery";
    }

    @GetMapping("/discovery-forget")
    public String onDiscoveryForget(@RequestParam String macAddress, Model model) {
        cs.forget(macAddress);
        return "redirect:/discovery";
    }
}
