package kg16.demo.model.services;

import java.sql.Timestamp;
import java.time.LocalDateTime;
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
                    INSERT INTO ToBeSentEMail (mail_address, user_name, type, mac_address, last_seen )  
                    SELECT  cl.mail_address, cl.user_name, cl.type, ?, ? 
                    FROM ContactList AS cl;
               """;
        /* Example for sms      
        String sql2 = """                
                    INSERT INTO ToBeSentSMS (sms, user_name, type, mac_address, last_seen )  
                    SELECT  cl.sms, cl.user_name, cl.type, ?, ? 
                    FROM ContactList AS cl;
                """;*/
        try {
            jdbc.update(sql, mac, last);
        } catch (DataAccessException e) {
            logger.error("Failed to update send queue for " + mac, e);
        }
    }

    /*public void SendSMS (String mac, String name, Timestamp last) {

    } */

    public void SendEmail(String mac, String name, Timestamp last) {

        
        
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

    

}
