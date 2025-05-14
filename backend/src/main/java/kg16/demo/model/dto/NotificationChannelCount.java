package kg16.demo.model.dto;

/**
 * Represents the count of notifications sent through a specific channel or type.
 * Used for visualizing or analyzing how different notification methods are used (e.g., Email, SMS, Slack).
 *
 * @param type   the type or channel of the notification (e.g., "Email", "SMS", "Slack")
 * @param count  the number of notifications sent through this channel
 */
public record NotificationChannelCount(String type, int count) {}
