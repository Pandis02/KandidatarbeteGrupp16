package kg16.demo.model.services;

import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import kg16.demo.model.exceptions.UserFacingException;

@Service
public class LocationsService {
    private final JdbcTemplate jdbc;

    public LocationsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void add(String building, String room) throws UserFacingException {
        try {
            Integer exists = jdbc.queryForObject(
                    "SELECT 1 FROM Locations WHERE building = ? AND room = ?",
                    Integer.class,
                    building,
                    room);

            // If it gets here, the location already exists
            throw new UserFacingException("Location already exists.");
        } catch (EmptyResultDataAccessException e) {
            // No existing location found, safe to insert
            jdbc.update("INSERT INTO Locations (building, room) VALUES (?, ?)", building, room);
        }
    }

    public void remove(Long id) {
        jdbc.update("DELETE FROM Locations WHERE location_id = ?", id);
    }

    public void edit(Long id, String building, String room) throws UserFacingException {
        try {
            Integer exists = jdbc.queryForObject(
                    "SELECT 1 FROM Locations WHERE building = ? AND room = ? AND location_id != ?",
                    Integer.class, building, room, id);

            throw new UserFacingException("Another location with the same building and room already exists.");
        } catch (EmptyResultDataAccessException e) {
            // No duplicates, proceed to update
            jdbc.update("UPDATE Locations SET building = ?, room = ? WHERE location_id = ?",
                    building, room, id);
        }
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