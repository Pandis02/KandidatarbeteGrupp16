package kg16.demo.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kg16.demo.model.Services.CheckinService;

@Controller
public class ViewCheckins {
    private final CheckinService checkinService;

    public ViewCheckins(CheckinService checkinService) {
        this.checkinService = checkinService;
    }

    @GetMapping("/checkins")
    public String onViewCheckins(@RequestParam(required = false) Integer old, Model model) {
        if (old == null)
            model.addAttribute("checkins", checkinService.get_all_checkins());
        else
            model.addAttribute("checkins", checkinService.get_all_checkins_older_than(old));

        return "checkins";
    }
}
