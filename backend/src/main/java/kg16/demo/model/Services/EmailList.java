package kg16.demo.model.Services;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Entity
@Table(name = "EmailList")
public class EmailList {

    @Id
    @Column(name = "mailAddress", nullable = false)
    private String mailAddress;

    @Column(name = "user")
    private String user;

    @Column(name = "sms")
    private String sms;

    @Column(name = "type", nullable = false)
    private String type;

    // Getters and Setters
    public String getMailAddress() {
        return mailAddress;
    }

    public void setMailAddress(String mailAddress) {
        this.mailAddress = mailAddress;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSms() {
        return sms;
    }

    public void setSms(String sms) {
        this.sms = sms;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

