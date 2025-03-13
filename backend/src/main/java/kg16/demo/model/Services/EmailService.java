package kg16.demo.model.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    public void sendAlarmEmail(String to, String variable1, String variable2, String variable3) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject("⚠️ Alarm Triggered!");
        helper.setFrom("your-email@example.com");

        // Prepare Thymeleaf context
        Context context = new Context();
        context.setVariable("variable1", variable1);
        context.setVariable("variable2", variable2);
        context.setVariable("variable3", variable3);

        // Process Thymeleaf template
        String htmlContent = templateEngine.process("alarm-email", context);

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}
