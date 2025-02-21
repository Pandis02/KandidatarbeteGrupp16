package kg16.demo.web;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kg16.demo.model.dto.LogDTO;
import kg16.demo.repository.LogRepository;

@RestController
@RequestMapping("/logs")
public class LogController {

    private static final Logger logger = LoggerFactory.getLogger(LogController.class);
    private final LogRepository logRepository;

    public LogController(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @GetMapping
    public ResponseEntity<List<LogDTO>> getLogs(@RequestParam(required = false) String deviceId,
                                                @RequestParam(required = false) String startDate,
                                                @RequestParam(required = false) String endDate,
                                                @RequestParam(required = false) String alertType) {

        Integer parsedAlertType = null;
        if (alertType != null) {
            try {
                parsedAlertType = Integer.parseInt(alertType);
            } catch (NumberFormatException e) {
                logger.error("Invalid alertType format: {}", alertType);
                return ResponseEntity.badRequest().build();
            }
        }

        Integer parsedDeviceID = null;
        if (deviceId != null) {
            try {
                parsedDeviceID = Integer.parseInt(deviceId);
            } catch (NumberFormatException e) {
                logger.error("Invalid deviceId format: {}", deviceId);
                return ResponseEntity.badRequest().build();
            }
        }

        List<LogDTO> logs = logRepository.findLogs(parsedDeviceID, startDate, endDate, parsedAlertType);
        return ResponseEntity.ok(logs);
    }
}
