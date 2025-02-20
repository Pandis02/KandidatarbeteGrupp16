package kg16.demo.dto;

import java.time.Instant;

public record ScanResponse(
    String message,
    String macAddress,
    Instant timestamp
) {}
