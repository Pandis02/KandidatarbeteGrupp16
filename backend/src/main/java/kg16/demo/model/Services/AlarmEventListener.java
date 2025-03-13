package kg16.demo.model.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import jakarta.mail.MessagingException;
import java.util.List;

@Component
public class AlarmEventListener {

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailListRepository emailListRepository;

    @EventListener
    public void handleAlarmTriggeredEvent(AlarmTriggeredEvent event) {
        List<String> emailAddresses = emailListRepository.findAllEmails();

        for (String recipient : emailAddresses) {
            try {
                emailService.sendAlarmEmail(
                    recipient,
                    event.getVariable1(),
                    event.getVariable2(),
                    event.getVariable3()
                );
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }
}
