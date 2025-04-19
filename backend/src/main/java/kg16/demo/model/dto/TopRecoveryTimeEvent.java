package kg16.demo.model.dto;

public class TopRecoveryTimeEvent {
    private final String macAddress;
    private final long recoveryTimeSeconds;
    private final String restoredAtFormatted;

    public TopRecoveryTimeEvent(String macAddress, long recoveryTimeSeconds, String restoredAtFormatted) {
        this.macAddress = macAddress;
        this.recoveryTimeSeconds = recoveryTimeSeconds;
        this.restoredAtFormatted = restoredAtFormatted;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public long getRecoveryTimeSeconds() {
        return recoveryTimeSeconds;
    }

    public String getRestoredAtFormatted() {
        return restoredAtFormatted;
    }
    public String getFormatted() {
        long seconds = recoveryTimeSeconds;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
    
        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }
    
}

