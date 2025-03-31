package kg16.demo.model.services;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import kg16.demo.model.Services.AlarmEventListener;
import kg16.demo.model.dto.LogDTO;
import kg16.demo.model.records.ScanRecord;

@Service
public class NotificationService {

    private final EmailService es;
    private final JdbcTemplate jdbc;
    private final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public NotificationService(JdbcTemplate jdbc, EmailService es) {
        this.jdbc = jdbc;
        this.es = es;
    }

    public void UpdateToSend(String mac, Timestamp last) {
        String sql = """
                     INSERT INTO ToBeSentEMail (recipient_value, mac_address, last_seen )
                     SELECT  rec.recipient_value, ?, ?
                     FROM Recipients AS rec
                     WHERE rec.recipient_type = 'email';
                """;
        // version 2 that incorporates groups
        /*
         * String sql = """
         * INSERT INTO ToBeSentEMail (recipient_value, mac_address, last_seen )
         * SELECT gcv.recipient_value, ?, ?
         * FROM GroupContactView AS gcv
         * WHERE (gcv.location_id = ?) AND (gcv.recipient_type = 'email') ;
         * """;
         */

        /*
         * Example for sms
         * String sql2 = """
         * INSERT INTO ToBeSentSMS (recipient_value, mac_address, last_seen )
         * SELECT rec.recipient_value, ?, ?
         * FROM Recipients AS rec
         * WHERE rec.recipient_type = 'sms';
         * """;
         */
        try {
            jdbc.update(sql, mac, last);
        } catch (DataAccessException e) {
            logger.error("Failed to update send queue for " + mac, e);
        }
    }

    public void SendEmail() {
        List<EmailToSend> mailList = getAllEmailToSend();
        String sql = """
                    DELETE FROM TOBESENTEMAIL;
                """;
        try {
            jdbc.execute(sql);
        } catch (DataAccessException e) {
            logger.error("Failed to clear ToBeSentEMail table", e);
        }
        String cmail = mailList.get(0).mailAddress;
        List<String> offline = new ArrayList<>();
        for (EmailToSend mail : mailList) {
            if (!cmail.equals(mail.mailAddress)) {
                es.sendAlarmEmail(cmail, String.join(", ", offline), "TestRoom", "TestBuilding");
                cmail = mail.mailAddress;
                offline.clear();
            }

            offline.add(mail.macAddress);
        }
        es.sendAlarmEmail(cmail, String.join(", ", offline), "TestRoom", "TestBuilding");

    }

    private List<EmailToSend> getAllEmailToSend() {
        String sql = """
                    SELECT recipient_value, mac_address, last_seen
                    FROM ToBeSentEMail se
                    ORDER BY recipient_value;
                """;

        return jdbc.query(String.format(sql), (r, rowNum) -> {
            return new EmailToSend(
                    r.getString("recipient_value"),
                    r.getString("mac_address"),
                    r.getTimestamp("last_seen"));
        });
    }

    public void NotificationLog(String mac, String message) {
        String sql = """
                    INSERT INTO NotificationEvents (mac_address, message)
                        VALUES (?, ?);
                    );
                """;
        try {
            jdbc.update(sql, mac, message);
        } catch (DataAccessException e) {
            logger.error("Failed to add notification message (" + message + ") for " + mac, e);
        }
    }

    public record EmailToSend(
            String mailAddress,
            String macAddress,
            Timestamp lastSeen) {
    }

}
