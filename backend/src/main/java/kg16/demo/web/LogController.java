package kg16.demo.web;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class LogController {

    @GetMapping("/logs")
    public List<Map<String, Object>> getLogs(@RequestParam(required = false) String deviceId,
                                             @RequestParam(required = false) String startDate,
                                             @RequestParam(required = false) String endDate,
                                             @RequestParam(required = false) String alertType) {
        List<Map<String, Object>> results = new ArrayList<>();
        String query = "SELECT * FROM OfflineEvents JOIN Scans ON OfflineEvents.device_id = Scans.device_id WHERE 1=1";

        List<Object> params = new ArrayList<>();

        if (deviceId != null) {
            query += " AND Scans.device_id = ?";
            params.add(deviceId);
        }

        if (startDate != null) {
            query += " AND OfflineEvents.offline_since >= ?";
            params.add(startDate);
        }

        if (endDate != null) {
            query += " AND OfflineEvents.offline_since <= ?";
            params.add(endDate);
        }

        if (alertType != null) {
            query += " AND Scans.status = ?";
            params.add(Integer.parseInt(alertType));
        }
        // H2 JDBC connection URL for file-based database
        try (Connection conn = DriverManager.getConnection("jdbc:h2:./store;DB_CLOSE_ON_EXIT=TRUE", "sa", "");
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set parameters for the prepared statement
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            // Execute query and process the result set
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> log = new HashMap<>();
                log.put("device_id", rs.getInt("device_id"));
                log.put("hostname", rs.getString("hostname"));
                log.put("ip_address", rs.getString("ip_address"));
                log.put("mac_address", rs.getString("mac_address"));
                log.put("status", rs.getInt("status"));
                log.put("last_seen", rs.getString("last_seen"));
                log.put("offline_since", rs.getString("offline_since"));
                log.put("restored_at", rs.getString("restored_at"));

                results.add(log);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }
}