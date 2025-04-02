package kg16.demo.model.services;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class LocationsService {
    private final JdbcTemplate jdbc;

    public LocationsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Location> getAllLocations() {
        return jdbc.query("SELECT * FROM Locations;", (rs, rowNum) -> new Location(
                rs.getInt("location_id"),
                rs.getString("building"),
                rs.getString("room")));
    }

    public record Location(int id, String building, String room) {
    }
}