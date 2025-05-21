package kg16.demo.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import kg16.demo.model.dto.*;
import kg16.demo.model.services.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Controller responsible for rendering the system statistics dashboard.
 * Gathers all analytical data for display including trends, distributions,
 * recovery performance, and false positive estimations.
 */
@Controller
public class ViewStatistics {

    private final BasicStatsService basicStats;
    private final TagStatsService tagStats;
    private final LocationStatsService locationStats;
    private final DowntimeStatsService downtimeStats;
    private final NotificationStatsService notificationStats;
    private final DeviceStatsService deviceStats;
    private final OfflineEventStatsService offlineEventStats;

    /**
     * Constructs the statistics controller with all required service dependencies.
     */
    public ViewStatistics(BasicStatsService basicStats,
                          TagStatsService tagStats,
                          LocationStatsService locationStats,
                          DowntimeStatsService downtimeStats,
                          NotificationStatsService notificationStats,
                          DeviceStatsService deviceStats,
                          OfflineEventStatsService offlineEventStats) {
        this.basicStats = basicStats;
        this.tagStats = tagStats;
        this.locationStats = locationStats;
        this.downtimeStats = downtimeStats;
        this.notificationStats = notificationStats;
        this.deviceStats = deviceStats;
        this.offlineEventStats = offlineEventStats;
    }

    /**
     * Displays the statistics dashboard populated with aggregated data
     * based on an optional date range. If no range is given, the last 7 days are used.
     *
     * @param from  optional start date in YYYY-MM-DD format
     * @param to    optional end date in YYYY-MM-DD format
     * @param model the Thymeleaf model used to populate the view
     * @return the name of the view template ("statistics")
     * @throws JsonProcessingException if any serialization errors occur (typically unused)
     */
    @GetMapping("/statistics")
    public String showStatisticsPage(@RequestParam(required = false) String from,
                                     @RequestParam(required = false) String to,
                                     Model model) throws JsonProcessingException {

        // Default date range: past 7 days
        LocalDateTime fromDate = (from != null && !from.isEmpty())
                ? LocalDate.parse(from).atStartOfDay()
                : LocalDate.now().minusDays(7).atStartOfDay();

        LocalDateTime toDate = (to != null && !to.isEmpty())
                ? LocalDate.parse(to).atTime(23, 59, 59)
                : LocalDate.now().atTime(23, 59, 59);

        // Core event stats
        List<DayCount> dailyEvents = basicStats.findEventsByDayBetween(fromDate, toDate);
        List<HourCount> hourlyEvents = basicStats.findEventsByHourBetween(fromDate, toDate);
        List<TagCount> tagCounts = tagStats.findCommonTagsBetween(fromDate, toDate);
        List<TopDevice> topDevices = downtimeStats.findMostProblematicDevicesBetween(fromDate, toDate);
        int totalEventCount = basicStats.countEventsBetween(fromDate, toDate);
        OptionalDouble avgDowntime = downtimeStats.findAverageDowntimeBetween(fromDate, toDate);
        int currentlyOffline = basicStats.countCurrentlyOfflineDevices();
        int totalDevices = deviceStats.getTotalTrackedDevices();
        double offlineRate = totalDevices > 0 ? (currentlyOffline * 100.0 / totalDevices) : 0.0;

        // Location-based and recovery insights
        List<LocationCount> locationCounts = locationStats.findEventsByLocationBetween(fromDate, toDate);
        List<AverageRestoreByLocation> avgRestoreByLocation = locationStats.findAverageRestoreTimesPerLocation(fromDate, toDate);
        List<TopRecoveryTimeEvent> topRecoveryTimeEvents = downtimeStats.findTopRecoveryTimeEvents(fromDate, toDate);

        // Distribution and trend data
        List<WeekdayCount> weekdayCounts = basicStats.findEventCountByWeekday(fromDate, toDate);
        List<DowntimeBucket> downtimeHistogram = downtimeStats.getDowntimeHistogram(fromDate, toDate);
        List<TagTrend> tagTrends = tagStats.getTagTrends(fromDate, toDate);

        // Notification-related stats
        List<MostNotifiedDevice> mostNotified = notificationStats.findMostNotifiedDevices(fromDate, toDate);
        List<NotificationChannelCount> channelCounts = notificationStats.getNotificationChannelDistributionBetween(fromDate, toDate);

        // False positive and device insights
        FalsePositiveSummary missedSummary = offlineEventStats.getCurrentMissedCheckinStats();
        List<DeviceStability> stabilityList = deviceStats.computeDeviceStability(fromDate, toDate);
        List<String> silentDevices = deviceStats.findDevicesWithNoEventsBetween(fromDate, toDate);

        // Add all data to the view model
        model.addAttribute("from", fromDate);
        model.addAttribute("to", toDate);
        model.addAttribute("dailyEvents", dailyEvents);
        model.addAttribute("hourlyEvents", hourlyEvents);
        model.addAttribute("tagCounts", tagCounts);
        model.addAttribute("topDevices", topDevices);
        model.addAttribute("avgDowntime", avgDowntime.isPresent() ? avgDowntime.getAsDouble() : null);
        model.addAttribute("totalEventCount", totalEventCount);
        model.addAttribute("currentlyOffline", currentlyOffline);
        model.addAttribute("locationCounts", locationCounts);
        model.addAttribute("topRecoveryTimeEvents", topRecoveryTimeEvents);
        model.addAttribute("weekdayCounts", weekdayCounts);
        model.addAttribute("downtimeHistogram", downtimeHistogram);
        model.addAttribute("tagTrends", tagTrends);
        model.addAttribute("mostNotifiedDevices", mostNotified);
        model.addAttribute("avgRestoreByLocation", avgRestoreByLocation);
        model.addAttribute("notificationChannels", channelCounts);
        model.addAttribute("missedCheckinSummary", missedSummary);
        model.addAttribute("deviceStability", stabilityList);
        model.addAttribute("totalDevices", totalDevices);
        model.addAttribute("offlineRate", offlineRate);
        model.addAttribute("silentDevices", silentDevices);

        return "statistics";
    }
}
