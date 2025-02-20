package kg16.demo.web;

import kg16.demo.dto.ScanDTO;
import kg16.demo.model.Scan;
import kg16.demo.repository.ScanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.Optional;

@RestController
@RequestMapping("/api/scans")
public class ScanController {

    @Autowired
    private ScanRepository scanRepository;

    @PostMapping("/add")
    public ResponseEntity<String> addOrUpdateScan(@RequestBody ScanDTO scanDTO) {
        Optional<Scan> existingScan = scanRepository.findByMacAddress(scanDTO.getMacAddress());

        if (existingScan.isPresent()) {
            scanRepository.updateScan(
                scanDTO.getMacAddress(),
                scanDTO.getIpAddress(),
                scanDTO.getHostname(),
                scanDTO.getStatus()
            );
            return ResponseEntity.ok("Updated scan for MAC: " + scanDTO.getMacAddress());
        } else {
            Scan newScan = new Scan(
                scanDTO.getHostname(),
                scanDTO.getIpAddress(),
                scanDTO.getMacAddress(),
                scanDTO.getStatus(),
                new Timestamp(System.currentTimeMillis())  // Set last seen to now
            );
            scanRepository.save(newScan);
            return ResponseEntity.ok("Inserted new scan for MAC: " + scanDTO.getMacAddress());
        }
    }
}
