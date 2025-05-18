package kg16.demo.model.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for manually triggering a test alarm.
 * This is typically used for development or integration testing of the alarm notification system.
 */
@RestController
public class AlarmTestController {

    /** Publishes application events, such as custom alarms. */
    @Autowired
    private ApplicationEventPublisher publisher;

    /**
     * Triggers a test alarm event to simulate an alarm notification.
     * This will publish an {@link AlarmTriggeredEvent} with dummy values.
     *
     * @return a confirmation message indicating the alarm was triggered
     */
    @GetMapping("/test-alarm")
    public String triggerTestEmail() {
        publisher.publishEvent(new AlarmTriggeredEvent(
            "Temp High",       // alarm type
            "Sensor A",        // source
            "Threshold Exceeded" // reason
        ));
        return "Alarm email triggered!";
    }
}
