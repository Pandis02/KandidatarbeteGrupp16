package kg16.demo.model.services;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AlarmEventListener {

    @Autowired
    private EmailService emailService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @EventListener
    public void handleAlarmTriggeredEvent(AlarmTriggeredEvent event) {
        List<String> emailAddresses = jdbcTemplate.queryForList(
                "SELECT MailAddress FROM EmailList", String.class);

        for (String recipient : emailAddresses) {
            try {
                emailService.sendAlarmEmail(
                        recipient,
                        event.getVariable1(),
                        event.getVariable2(),
                        event.getVariable3());
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }
}
