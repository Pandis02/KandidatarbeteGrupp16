package kg16.demo.web;

import kg16.demo.dto.ScanDTO;
import kg16.demo.dto.ScanResponse;
import kg16.demo.model.Database.Database;
import kg16.demo.model.records.ScanRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;


@RestController
@RequestMapping("/api/scans")  // Ensure API URLs are prefixed with /api
public class ScanController {

    private final Database database;

    @Autowired
    public ScanController(Database database) {
        this.database = database;
    }

    @PostMapping("/add")
    public ResponseEntity<ScanResponse> addOrUpdateScan(@RequestBody ScanDTO scan) {
        database.upsertScan(scan.getHostname(), scan.getIpAddress(), scan.getMacAddress(), scan.getStatus());
        return ResponseEntity.ok(new ScanResponse(
            "Scan recorded for MAC: " + scan.getMacAddress(),
            scan.getMacAddress(),
            Instant.now()
        ));
    }

    @GetMapping  // Fetch all scans
    public ResponseEntity<List<ScanRecord>> getAllScans() {
        return ResponseEntity.ok(database.getAllScans());
    }

    @GetMapping("/old/{minutes}")  // Fetch old scans
    public ResponseEntity<List<ScanRecord>> getOldScans(@PathVariable int minutes) {
        return ResponseEntity.ok(database.getOldScans(minutes));
    }
}
