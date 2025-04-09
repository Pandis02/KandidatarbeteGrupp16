package kg16.demo.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kg16.demo.model.exceptions.UserFacingException;
import kg16.demo.model.services.LocationsService;

@Controller
public class ViewLocations {
    private final LocationsService ls;

    public ViewLocations(LocationsService ls) {
        this.ls = ls;
    }

    @GetMapping("/locations")
    public String showLocations(Model model) {
        model.addAttribute("locations", ls.getAllLocations());
        return "locations";
    }

    @PostMapping("/add-location")
    public String addLocation(@RequestParam String building, @RequestParam String room, RedirectAttributes rda) {

        try {
            ls.add(building, room);
        } catch (UserFacingException e) {
            rda.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/locations";
    }

    @GetMapping("/delete-location")
    public String deleteLocation(@RequestParam Long id) {
        ls.remove(id);
        return "redirect:/locations";
    }

    @PostMapping("/edit-location")
    public String editLocation(
            @RequestParam Long id,
            @RequestParam String building,
            @RequestParam String room,
            RedirectAttributes rda) {

        try {
            ls.edit(id, building, room);
        } catch (UserFacingException e) {
            rda.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/locations";
    }
}