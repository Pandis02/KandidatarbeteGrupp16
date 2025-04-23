package kg16.demo.model.services;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import kg16.demo.model.dto.*;

import java.time.*;

@Service
public class CheckinStatsService {
    
    private final JdbcTemplate jdbc;

    public CheckinStatsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public MissedCheckinSummary getMissedCheckinStatsBetween(LocalDateTime from, LocalDateTime to) {
        // Tröskelvärde från inställningar
        int alertThresholdMin = jdbc.queryForObject(
            "SELECT alert_threshold_minutes FROM AdminSettings WHERE id = 1",
            Integer.class
        );
        int gracePeriodMinutes = alertThresholdMin + 1;
    
        LocalDateTime lateCutoff = LocalDateTime.now().minusMinutes(gracePeriodMinutes);
    
        // Antal checkins i valt intervall
        int totalCheckins = jdbc.queryForObject("""
            SELECT COUNT(*) FROM Checkins
            WHERE last_checkin BETWEEN ? AND ?
        """, Integer.class, from, to);
    
        // Hur många var sena (baserat på nuvarande cutoff)
        int lateCheckins = jdbc.queryForObject("""
            SELECT COUNT(*) FROM Checkins
            WHERE last_checkin BETWEEN ? AND ? AND last_checkin < ?
        """, Integer.class, from, to, lateCutoff);
    
        // Offline events under perioden
        int recentOfflineEvents = jdbc.queryForObject("""
            SELECT COUNT(*) FROM OfflineEvents
            WHERE offline_since BETWEEN ? AND ?
        """, Integer.class, from, to);
    
        return new MissedCheckinSummary(totalCheckins, lateCheckins, recentOfflineEvents);
    }
   
}
