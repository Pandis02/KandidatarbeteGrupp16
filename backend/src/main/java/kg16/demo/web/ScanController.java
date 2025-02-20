package kg16.demo.web;

import kg16.demo.dto.ScanResponse;
import kg16.demo.model.Scan;
import kg16.demo.repository.ScanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.sql.Timestamp;

@RestController
@RequestMapping("/api/scans")
public class ScanController {

    @Autowired
    private ScanRepository scanRepository;

    @PostMapping("/add")
    public ResponseEntity<ScanResponse> addOrUpdateScan(@RequestBody Scan scan) {
        Optional<Scan> existingScan = scanRepository.findByMacAddress(scan.getMacAddress());

        if (existingScan.isPresent()) {
            scanRepository.updateScan(scan.getMacAddress(), scan.getIpAddress(), scan.getHostname(), scan.getStatus());
            return ResponseEntity.ok(new ScanResponse(
                "Updated scan for MAC: ",
                scan.getMacAddress(),
                Instant.now()
            ));
        } else {
            scan.setLastSeen(new Timestamp(System.currentTimeMillis()));
            scanRepository.save(scan);
            return ResponseEntity.ok(new ScanResponse(
                "Inserted new scan for MAC: ",
                scan.getMacAddress(),
                Instant.now()
            ));
        }
    }
}
