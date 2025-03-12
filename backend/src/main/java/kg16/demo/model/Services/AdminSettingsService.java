package kg16.demo.model.services;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;
import kg16.demo.model.dto.AdminSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminSettingsService {
    private final JdbcTemplate jdbc;
    private static final Logger logger = LoggerFactory.getLogger(AdminSettingsService.class);

    public AdminSettingsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public AdminSettings getSettings() {
        String sql = "SELECT * FROM AdminSettings WHERE id = 1";
        try {
            return jdbc.queryForObject(sql, (rs, rowNum) -> new AdminSettings(
                rs.getInt("alert_threshold_minutes"),
                rs.getInt("checkin_interval_seconds"),
                rs.getTimestamp("last_updated")
            ));
        } catch (EmptyResultDataAccessException e) {
            logger.error("No AdminSettings found for id 1");
            throw new IllegalStateException("Admin settings not found", e);
        }
    }
    
}
