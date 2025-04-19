package kg16.demo.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;

@Getter
@Setter
@Entity
public class OfflineEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    private String macAddress;
    private String location;
    private LocalDateTime offlineSince;
    private LocalDateTime restoredAt;
    private String tag;

    // Getters and Setters
}

