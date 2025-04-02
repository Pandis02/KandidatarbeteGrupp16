package kg16.demo.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import kg16.Utils;
import kg16.demo.model.services.AdminSettingsService;
import kg16.demo.model.services.CheckinService;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class CheckinController {
    private final CheckinService checkinService;
    private final AdminSettingsService assc;
    private static final Logger logger = LoggerFactory.getLogger(CheckinController.class);

    public CheckinController(CheckinService checkinService, AdminSettingsService assc) {
        this.checkinService = checkinService;
        this.assc = assc;
    }

    @PostMapping("/checkin")
    public Map<String, Object> onCheckIn(@RequestBody BodyOfCheckIn body) {
        if (!body.isValid()) {
            logger.error("Invalid check-in request: " + body);
            return Map.of("success", false, "message", "invalid mac");
        }

        checkinService.upsertCheckin(body.macAddress(), body.hostname());
        logger.info(body.macAddress() + " has checked in!");
        return Map.of("success", true, "interval", assc.getSettings().getCheckinIntervalSeconds());
    }
}

record BodyOfCheckIn(String macAddress, String hostname) {
    boolean isValid() {
        return macAddress != null && macAddress.length() == 17 && Utils.isValidMacAddress(macAddress);
    }
}
