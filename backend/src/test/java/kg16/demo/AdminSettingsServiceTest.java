package kg16.demo;

import kg16.demo.model.services.AdminSettingsService;
import kg16.demo.model.dto.AdminSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the AdminSettingsService class.
 */
public class AdminSettingsServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private AdminSettingsService adminSettingsService;

    /**
     * Sets up the test environment by initializing mocks and the service instance.
     */
    @BeforeEach
    public void setUp() {
        // Initialize the mocks
        MockitoAnnotations.openMocks(this);
        // Instantiate the service with the mocked JdbcTemplate
        adminSettingsService = new AdminSettingsService(jdbcTemplate);
    }

    /**
     * Tests the getSettings method of AdminSettingsService.
     * Verifies that the method returns the correct AdminSettings object.
     */
    @Test
    public void testGetSettings() {
        // Prepare mock data
        AdminSettings mockSettings = new AdminSettings(3, 15, null); 
    
        // Mock JdbcTemplate behavior
        when(jdbcTemplate.queryForObject(
                eq("SELECT * FROM AdminSettings WHERE id = 1"),
                any(RowMapper.class)
        )).thenReturn(mockSettings);
    
        // Call the method and check the result
        AdminSettings result = adminSettingsService.getSettings();
    
        // Verify behavior
        assertNotNull(result); 
        assertEquals(3, result.getAlertThresholdMinutes());
        assertEquals(15, result.getCheckinIntervalSeconds());
        assertNull(result.getLastUpdated());  
    }
}