package kg16.demo.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class MainController {
    public MainController() {
    }

    @GetMapping("/")
    public String onIndex() {
        return "redirect:/dashboard";
    }
}