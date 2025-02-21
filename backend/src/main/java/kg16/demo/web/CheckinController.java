package kg16.demo.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import kg16.Utils;
import kg16.demo.model.Services.CheckinService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class CheckinController {
    private final CheckinService checkinService;
    private static final Logger logger = LoggerFactory.getLogger(CheckinController.class);

    public CheckinController(CheckinService checkinService) {
        this.checkinService = checkinService;
    }

    @PostMapping("/checkin")
    public void onCheckIn(@RequestBody BodyOfCheckIn body) {
        if (!body.isValid()) {
            logger.error("Invalid check-in request: " + body);
            return;
        }

        checkinService.insert_checkin(body.macAddress());
        logger.info(body.macAddress() + " has checked in!");
    }
}

record BodyOfCheckIn(String macAddress) {
    boolean isValid() {
        return macAddress != null && macAddress.length() == 17 && Utils.isValidMacAddress(macAddress);
    }
}
