package kg16.demo.model.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import kg16.demo.model.services.AlarmTriggeredEvent;

@RestController
public class AlarmTestController {

    @Autowired
    private ApplicationEventPublisher publisher;

    @GetMapping("/test-alarm")
    public String triggerTestEmail() {
        publisher.publishEvent(new AlarmTriggeredEvent("Temp High", "Sensor A", "Threshold Exceeded"
        ));
        return "Alarm email triggered!";
    }
}
