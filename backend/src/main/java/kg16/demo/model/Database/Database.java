package kg16.demo.model.Database;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import kg16.demo.model.Records.Checkin;

@Service
public class Database {
    private final JdbcTemplate jdbc;

    public Database(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    // single query based insert/update
    public void insert_checkin(String mac_address) {
        String sql = """
                    MERGE INTO Checkins c
                    USING (SELECT '%s' AS mac_address, NOW() AS last_checkin) AS new_data
                    ON c.mac_address = new_data.mac_address
                    WHEN MATCHED THEN
                        UPDATE SET c.last_checkin = new_data.last_checkin
                    WHEN NOT MATCHED THEN
                        INSERT (mac_address, last_checkin) VALUES (new_data.mac_address, new_data.last_checkin);
                """;
        jdbc.execute(String.format(sql, mac_address));
    }

    // get all checkins
    public List<Checkin> get_all_checkins() {
        String sql = "SELECT * FROM Checkins;";

        return jdbc.query(sql, (r, rowNum) -> {
            return new Checkin(r.getString("mac_address"), r.getTimestamp("last_checkin"));
        });
    }

    // get all colums of mac_addresses that have not checked in, in the last x minutes
    public List<Checkin> get_all_checkins_older_than(int minutes) {
        LocalDateTime before = LocalDateTime.now().minusMinutes(minutes);
        String sql = "SELECT * FROM Checkins WHERE last_checkin < ?";

        return jdbc.query(sql, new Object[] { Timestamp.valueOf(before) },
                (rs, rowNum) -> new Checkin(rs.getString("mac_address"), rs.getTimestamp("last_checkin")));
    }
}