package kg16.demo.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import kg16.Utils;
import kg16.demo.model.Database.Database;

@RestController
public class CheckinController {
    private final Database db; 

    public CheckinController(Database db) {
        this.db = db;
    }

    @PostMapping("/checkin")
    public void onCheckIn(@RequestBody BodyOfCheckIn body) {
        if (!body.isValid()) {
            System.out.println("Invalid body recieved in /checkin: " + body);
            return;
        }

        db.insert_checkin(body.mac_address());
        System.out.println(body.mac_address() + " has checked in!");
    }
}

record BodyOfCheckIn(String mac_address) {
    boolean isValid() {
        return !Utils.isEmpty(mac_address) && mac_address.length() == 17 && Utils.isValidMacAddress(mac_address);
    }
}
