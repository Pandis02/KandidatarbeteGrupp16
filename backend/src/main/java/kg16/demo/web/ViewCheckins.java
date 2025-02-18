package kg16.demo.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kg16.demo.model.Database.Database;

@Controller
public class ViewCheckins {
    private final Database db;

    public ViewCheckins(Database db) {
        this.db = db;
    }

    @GetMapping("/checkins")
    public String onViewCheckins(@RequestParam(required = false) Integer old, Model model) {
        if (old == null)
            model.addAttribute("checkins", db.get_all_checkins());
        else
            model.addAttribute("checkins", db.get_all_checkins_older_than(old));

        return "checkins";
    }
}
