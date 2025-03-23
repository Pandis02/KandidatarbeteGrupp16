package kg16.demo.model.services;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import kg16.demo.model.Services.AlarmEventListener;
import kg16.demo.model.dto.LogDTO;
import kg16.demo.model.dto.MailDTO;
import kg16.demo.model.records.ScanRecord;

@Service
public class NotificationService {

    private final Services.AlarmEventListener ael;
    private final JdbcTemplate jdbc;

    public NotificationService(JdbcTemplate jdbc, Services.AlarmEventListener alarmEventListener) {
        this.jdbc = jdbc;
        this.ael = alarmEventListener;
    }

    public void UpdateToSend (String mac, Timestamp last) {
        String sql = """                
                    INSERT INTO ToBeSentEMail (mail_address, mac_address, last_seen )  
                    SELECT  el.mail_address, ?, ? 
                    FROM EmailList AS el;
               """;
        // version 2 that incorporates responsibilitygroups
        /*String sql = """                
                INSERT INTO ToBeSentEMail (mail_address, mac_address, last_seen )  
                SELECT  el.mail_address, ?, ? 
                FROM GroupsMail AS gm 
                INNER JOIN ResponsibilityGroups AS rg ON  rg.group = gm.group
                WHERE rg.building = ? ;
           """;*/


        /* Example for sms      
        String sql2 = """                
                    INSERT INTO ToBeSentSMS (sms, mac_address, last_seen )  
                    SELECT  el.sms, ?, ? 
                    FROM EmailList AS el;
                """;*/
        try {
            jdbc.update(sql, mac, last);
        } catch (DataAccessException e) {
            logger.error("Failed to update send queue for " + mac, e);
        }
    }

    /*public void SendSMS (String mac, String name, Timestamp last) {

    } */

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
                // send to email service the list and the current mail address cmail
                cmail = mail.mailAddress;
                offline.clear();
            }

            offline.add(mail.macAddress);
        }
        // send to email service the list and the current mail address cmail
        
    }

    private List<EmailToSend> getAllEmailToSend() {
        String sql = """
                    SELECT mail_address, user_name, type, mac_address, last_seen
                    FROM ToBeSentEMail se
                    ORDER BY mail_address ;
                """;

        return jdbc.query(String.format(sql), (r, rowNum) -> {
            return new EmailToSend(
                    r.getString("mail_address"),
                    r.getString("mac_address"),
                    r.getTimestamp("last_seen"));
        });
    }

    public void NotificationLog (String mac , String message) {
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
