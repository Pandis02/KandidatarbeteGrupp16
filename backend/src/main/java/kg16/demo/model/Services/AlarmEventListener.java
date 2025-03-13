package kg16.demo.model.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;

@Component
public class AlarmEventListener {

    @Autowired
    private EmailService emailService;

    @EventListener
    public void handleAlarmTriggeredEvent(AlarmTriggeredEvent event) {
        try {
            emailService.sendAlarmEmail(
                "recipient@example.com",
                event.getVariable1(),
                event.getVariable2(),
                event.getVariable3()
            );
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
