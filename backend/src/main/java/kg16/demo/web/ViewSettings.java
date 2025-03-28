package kg16.demo.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import kg16.demo.model.services.AdminSettingsService;
import kg16.demo.model.services.RecipientService;

@Controller
public class ViewSettings {
    private final AdminSettingsService settingsService;
    private final RecipientService recipientService;

    public ViewSettings(AdminSettingsService ass, RecipientService rs) {
        this.settingsService = ass;
        this.recipientService = rs;
    }

    @GetMapping("/settings")
    public String onDevices(Model model) {
        var settings = settingsService.getSettings();
        var recipients = recipientService.getAllRecipients();

        model.addAttribute("alertThresholdMinutes", settings.getAlertThresholdMinutes());
        model.addAttribute("checkinIntervalSeconds", settings.getCheckinIntervalSeconds());
        model.addAttribute("lastUpdated", settings.getLastUpdated());
        model.addAttribute("recipients", recipients);

        return "settings";
    }

    @GetMapping("/settings-set")
    public String onSetAlertThreshold(
            @RequestParam(required = false) Integer alertThresholdMinutes,
            @RequestParam(required = false) Integer checkinIntervalSeconds) {

        if (alertThresholdMinutes != null) {
            settingsService.setAlertThreshold(alertThresholdMinutes);
        }

        if (checkinIntervalSeconds != null) {
            settingsService.setCheckinInterval(checkinIntervalSeconds);
        }

        return "redirect:/settings";
    }
}