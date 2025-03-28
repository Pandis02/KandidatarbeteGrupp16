package kg16.demo.model.services;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;
import kg16.demo.model.dto.AdminSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service class for managing admin settings.
 */
@Service
public class AdminSettingsService {
    private final JdbcTemplate jdbc;
    private static final Logger logger = LoggerFactory.getLogger(AdminSettingsService.class);

    /**
     * Constructs an AdminSettingsService with the specified JdbcTemplate.
     *
     * @param jdbc the JdbcTemplate to use for database operations
     */
    public AdminSettingsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Retrieves the admin settings from the database.
     *
     * @return the AdminSettings object containing the settings
     * @throws IllegalStateException if no settings are found for the specified id
     */
    public AdminSettings getSettings() {
        String sql = "SELECT * FROM AdminSettings WHERE id = 1;";
        try {
            return jdbc.queryForObject(sql, (rs, rowNum) -> new AdminSettings(
                    rs.getInt("alert_threshold_minutes"),
                    rs.getInt("checkin_interval_seconds"),
                    rs.getTimestamp("last_updated")));
        } catch (EmptyResultDataAccessException e) {
            logger.error("No AdminSettings found for id 1");
            throw new IllegalStateException("Admin settings not found", e);
        }
    }

    public void setAlertThreshold(int threshold) {
        String sql = "UPDATE AdminSettings SET alert_threshold_minutes = ? WHERE id = 1;";
        if (jdbc.update(sql, threshold) != 1) {
            logger.error("Column 'alert_threshold_minutes' could not be updated. Make sure a row exists.");
        }
    }

    public void setCheckinInterval(int interval) {
        String sql = "UPDATE AdminSettings SET checkin_interval_seconds = ? WHERE id = 1;";
        if (jdbc.update(sql, interval) != 1) {
            logger.error("Column 'checkin_interval_seconds' could not be updated. Make sure a row exists.");
        }
    }
}