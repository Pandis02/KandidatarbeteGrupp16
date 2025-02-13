package kg16.demo.web;

import org.springframework.web.bind.annotation.*;

@RestController
public class MainController {
    public MainController() {
    }

    @GetMapping("/")
    public String onIndex() {
        return "Hello World";
    }
}