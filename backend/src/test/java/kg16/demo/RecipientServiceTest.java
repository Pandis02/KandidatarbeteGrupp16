package kg16.demo;

import kg16.demo.model.dto.Recipient;
import kg16.demo.model.dto.Role;
import kg16.demo.model.exceptions.DuplicateException;
import kg16.demo.model.services.RecipientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.springframework.jdbc.core.RowMapper;

/**
 * Unit tests for the RecipientService class.
 */
public class RecipientServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private RecipientService recipientService;

    /**
     * Sets up the test environment before each test.
     */
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Tests the addRecipient method when the recipient does not already exist.
     */
    @Test
    public void testAddRecipient() {
        String type = "email";
        String value = "test@example.com";

        // Mock the query to check if the recipient already exists
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(value))).thenReturn(0);

        // Mock the update query to insert the new recipient
        when(jdbcTemplate.update(anyString(), eq(type), eq(value))).thenReturn(1);

        // Call the method to test
        try {
            Recipient recipient = recipientService.addRecipient(type, value);
            // Verify the interactions with the mock
            verify(jdbcTemplate, times(1)).queryForObject(anyString(), eq(Integer.class), eq(value));
            verify(jdbcTemplate, times(1)).update(anyString(), eq(type), eq(value));

            // Assert the result
            assertNotNull(recipient);
            assertNull(recipient.getRecipientId()); // Assuming recipient_id is auto-incremented and not returned
            assertEquals(type, recipient.getRecipientType());
            assertEquals(value, recipient.getRecipientValue());
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * Tests the addRecipient method when the recipient already exists.
     */
    @Test
    public void testAddRecipientAlreadyExists() {
        String type = "email";
        String value = "test@example.com";

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString())).thenReturn(1);

        assertThrows(DuplicateException.class, () -> recipientService.addRecipient(type, value));
    }

    /**
     * Tests the getAllRecipients method.
     */
    @Test
    public void testGetAllRecipients() {
        List<Recipient> mockRecipients = new ArrayList<>();
        mockRecipients.add(new Recipient(1L, "email", "test@example.com", new ArrayList<>()));

        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(mockRecipients);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), anyLong())).thenReturn(new ArrayList<Role>());

        List<Recipient> recipients = recipientService.getAllRecipients();

        assertNotNull(recipients);
        assertEquals(1, recipients.size());
        assertEquals("test@example.com", recipients.get(0).getRecipientValue());
    }

    /**
     * Tests the deleteRecipient method.
     */
    @Test
    public void testDeleteRecipient() {
        when(jdbcTemplate.update(anyString(), anyLong())).thenReturn(1);

        recipientService.deleteRecipient(1L);

        verify(jdbcTemplate, times(1)).update(anyString(), eq(1L));
    }

    /**
     * Tests the assignRecipientToRole method when the role exists and is not yet assigned.
     */
    @Test
    public void testAssignRecipientToRole() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), anyString())).thenReturn(1L); // Role exists
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyLong(), anyLong())).thenReturn(0); // Role not yet assigned
        when(jdbcTemplate.update(anyString(), anyLong(), anyLong())).thenReturn(1); // Insert succeeds

        recipientService.assignRecipientToRole(1L, "ROLE_USER");

        verify(jdbcTemplate, times(1)).queryForObject(anyString(), eq(Long.class), eq("ROLE_USER"));
        verify(jdbcTemplate, times(1)).queryForObject(anyString(), eq(Integer.class), eq(1L), eq(1L));
        verify(jdbcTemplate, times(1)).update(anyString(), eq(1L), eq(1L));
    }

    /**
     * Tests the removeRecipientFromRole method.
     */
    @Test
    public void testRemoveRecipientFromRole() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), anyString())).thenReturn(1L);
        when(jdbcTemplate.update(anyString(), anyLong(), anyLong())).thenReturn(1);

        recipientService.removeRecipientFromRole(1L, "ROLE_USER");

        verify(jdbcTemplate, times(1)).queryForObject(anyString(), eq(Long.class), eq("ROLE_USER"));
        verify(jdbcTemplate, times(1)).update(anyString(), eq(1L), eq(1L));
    }

    /**
     * Tests the getRolesForRecipient method.
     */
    @Test
    public void testGetRolesForRecipient() {
        List<Role> mockRoles = new ArrayList<>();
        mockRoles.add(new Role(1L, "ROLE_USER"));

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), anyLong())).thenReturn(mockRoles);

        List<Role> roles = recipientService.getRolesForRecipient(1L);

        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("ROLE_USER", roles.get(0).getRoleName());
    }

    /**
     * Tests the assignRecipientToRole method when the role does not exist.
     */
    @Test
    public void testAssignRecipientToNonExistentRole() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), anyString()))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThrows(IllegalArgumentException.class,
                () -> recipientService.assignRecipientToRole(1L, "ROLE_NON_EXISTENT"));
    }

    /**
     * Tests the removeRecipientFromRole method when the role does not exist.
     */
    @Test
    public void testRemoveRecipientFromNonExistentRole() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), anyString()))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThrows(IllegalArgumentException.class,
                () -> recipientService.removeRecipientFromRole(1L, "ROLE_NON_EXISTENT"));
    }
}