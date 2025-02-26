package kg16.demo.web;

import kg16.demo.model.services.ScanService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scans")
public class ScanController {

    private final ScanService scanService;

    @Autowired
    public ScanController(ScanService scanService) {
        this.scanService = scanService;
    }

    @PostMapping("/add")
    public void addScan(@RequestBody ScanRequest scanRequest) {
        // Call the upsert method from ScanService
        scanService.upsertScan(scanRequest.getHostname(), scanRequest.getIpAddress(), scanRequest.getMacAddress());
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScanRequest {
        private String hostname;
        private String ipAddress;
        private String macAddress;
    }
}
