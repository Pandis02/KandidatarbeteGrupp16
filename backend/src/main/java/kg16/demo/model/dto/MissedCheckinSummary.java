package kg16.demo.model.dto;

public record MissedCheckinSummary(int totalCheckins, int lateCheckins, int offlineEventsLast24h) {
    public int estimatedFalsePositives() {
        return Math.max(lateCheckins - offlineEventsLast24h, 0);
    }
}

