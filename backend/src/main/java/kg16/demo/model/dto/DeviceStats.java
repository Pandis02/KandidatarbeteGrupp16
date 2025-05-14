package kg16.demo.model.dto;

/**
 * Represents aggregated statistics for a single device,
 * including its total number of offline events, how many
 * of those recovered quickly, and the quick recovery rate.
 *
 * Used to analyze device behavior and reliability over time.
 *
 * @param totalEvents         total number of offline events recorded for the device
 * @param quickRecoveries     number of events that were restored within a defined quick threshold
 * @param quickRecoveryRate   proportion of quick recoveries (range: 0.0 to 1.0)
 */
public record DeviceStats(
    int totalEvents,
    int quickRecoveries,
    double quickRecoveryRate
) {}
