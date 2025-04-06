package kg16.demo.model.services;

import java.util.*;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final JdbcTemplate jdbc;
    private final AdminSettingsService settings;

    public DashboardService(AdminSettingsService settings, JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
        this.settings = settings;
    }

    public DeviceStatusCount getCounts() {
        var minutes = settings.getSettings().getAlertThresholdMinutes();
        String sql = """
                SELECT
                    COUNT(td.mac_address) AS total,
                    COUNT(oe.mac_address) AS total_unreachable_devices_prior_alert_threshold,
                    COUNT(DISTINCT oe2.mac_address) AS total_unreachable_devices_past_alert_threshold
                FROM
                    TrackedDevices td
                LEFT JOIN
                    OfflineEvents oe ON td.mac_address = oe.mac_address AND oe.restored_at IS NULL AND oe.offline_since > NOW() - INTERVAL '%d' MINUTE
                LEFT JOIN
                    OfflineEvents oe2 ON td.mac_address = oe2.mac_address AND oe2.restored_at IS NULL AND oe2.offline_since < NOW() - INTERVAL '%d' MINUTE
                WHERE
                    td.enabled = TRUE;
                """;

        return jdbc.queryForObject(String.format(sql, minutes, minutes), (r, rowNum) -> {
            return new DeviceStatusCount(
                    r.getInt("total_unreachable_devices_prior_alert_threshold"),
                    r.getInt("total_unreachable_devices_past_alert_threshold"),
                    r.getInt("total"),
                    r.getInt("total") -
                            r.getInt("total_unreachable_devices_prior_alert_threshold") -
                            r.getInt("total_unreachable_devices_past_alert_threshold"));
        });
    }

    private List<Location> getLocationsBriefing() {
        var minutes = settings.getSettings().getAlertThresholdMinutes();
        String sql = """
                SELECT
                    loc.location_id,
                    loc.building,
                    loc.room,
                    COUNT(td.mac_address) AS total_devices,
                    COUNT(oe.mac_address) AS offline_devices
                FROM
                    Locations loc
                LEFT JOIN
                    TrackedDevices td on td.location_id = loc.location_id AND td.enabled
                LEFT JOIN
                    OfflineEvents oe ON td.mac_address = oe.mac_address AND oe.restored_at IS NULL
                    AND oe.offline_since < NOW() - INTERVAL '%d' MINUTE AND td.enabled
                GROUP BY
                    loc.location_id
                ORDER BY
                    loc.building ASC;
                """;

        return jdbc.query(String.format(sql, minutes), (r, rowNum) -> {
            return new Location(
                    r.getInt("location_id"),
                    r.getString("building"),
                    r.getString("room"),
                    r.getInt("total_devices"),
                    r.getInt("offline_devices"));
        });
    }

    public List<Building> getBuildingsInfo() {
        var locations = getLocationsBriefing();

        // Group locations by building name and transform each into Room objects
        Map<String, List<Room>> buildingToRooms = new HashMap<>();

        for (var location : locations) {
            Room room = new Room(
                    location.locationId(),
                    location.room(),
                    location.totalEnabledDevices(),
                    location.totalEnabledOffline(),
                    location.totalEnabledDevices() - location.totalEnabledOffline());

            String buildingName = location.building();

            // If we haven't seen this building before, create a new list
            if (!buildingToRooms.containsKey(buildingName)) {
                buildingToRooms.put(buildingName, new ArrayList<>());
            }

            // Add the room to the appropriate building
            buildingToRooms.get(buildingName).add(room);
        }

        // Transform the map into a list of Building objects
        List<Building> buildings = new ArrayList<>();
        for (Map.Entry<String, List<Room>> entry : buildingToRooms.entrySet()) {
            String buildingName = entry.getKey();
            List<Room> rooms = entry.getValue();

            // Calculate building-level totals
            int buildingTotalDevices = 0;
            int buildingOfflineDevices = 0;

            for (Room room : rooms) {
                buildingTotalDevices += room.totalDevices();
                buildingOfflineDevices += room.offlineDevices();
            }

            var onlineDevices = buildingTotalDevices - buildingOfflineDevices;

            Building building = new Building(rooms, buildingName, buildingTotalDevices, buildingOfflineDevices,
                    onlineDevices);
            buildings.add(building);
        }

        return buildings;
    }

    private record Location(
            int locationId,
            String building,
            String room,
            int totalEnabledDevices,
            int totalEnabledOffline) {
    }

    public record Room(
            int locationId,
            String name,
            int totalDevices,
            int offlineDevices,
            int onlineDevices) {
    }

    public record Building(
            List<Room> rooms,
            String name,
            int totalDevices,
            int offlineDevices,
            int onlineDevices) {
    }

    public record DeviceStatusCount(
            int tempOffline,
            int offline,
            int totalComputers,
            int online) {
    }

}
