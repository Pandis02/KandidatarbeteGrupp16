package kg16.demo.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import kg16.demo.model.dto.*;
import kg16.demo.model.services.BasicStatsService;
import kg16.demo.model.services.CheckinStatsService;
import kg16.demo.model.services.DeviceStatsService;
import kg16.demo.model.services.DowntimeStatsService;
import kg16.demo.model.services.LocationStatsService;
import kg16.demo.model.services.NotificationStatsService;
import kg16.demo.model.services.TagStatsService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;

@Controller
public class ViewStatistics {

    private final BasicStatsService basicStats;
    private final TagStatsService tagStats;
    private final LocationStatsService locationStats;
    private final DowntimeStatsService downtimeStats;
    private final NotificationStatsService notificationStats;
    private final DeviceStatsService deviceStats;
    private final CheckinStatsService checkinStats;

    public ViewStatistics(BasicStatsService basicStats, TagStatsService tagStats, LocationStatsService locationStats, DowntimeStatsService downtimeStats, NotificationStatsService notificationStats, DeviceStatsService deviceStats, CheckinStatsService checkinStats) {
        this.basicStats = basicStats;
        this.tagStats = tagStats;
        this.locationStats = locationStats;
        this.downtimeStats = downtimeStats;
        this.notificationStats = notificationStats;
        this.deviceStats = deviceStats;
        this.checkinStats = checkinStats;

    }

    @GetMapping("/statistics")
    public String showStatisticsPage(@RequestParam(required = false) String from,
                                     @RequestParam(required = false) String to,
                                     Model model) throws JsonProcessingException {
        LocalDateTime fromDate = (from != null && !from.isEmpty())
                ? LocalDate.parse(from).atStartOfDay()
                : LocalDate.now().minusDays(7).atStartOfDay();

        LocalDateTime toDate = (to != null && !to.isEmpty())
                ? LocalDate.parse(to).atTime(23, 59, 59)
                : LocalDate.now().atTime(23, 59, 59);

        // Fetch data for the charts and tables
        List<DayCount> dailyEvents = basicStats.findEventsByDayBetween(fromDate, toDate);
        List<HourCount> hourlyEvents = basicStats.findEventsByHourBetween(fromDate, toDate);
        List<TagCount> tagCounts = tagStats.findCommonTagsBetween(fromDate, toDate);
        List<TopDevice> topDevices = downtimeStats.findMostProblematicDevicesBetween(fromDate, toDate);
        int totalEventCount = basicStats.countEventsBetween(fromDate, toDate);
        OptionalDouble avgDowntime = downtimeStats.findAverageDowntimeBetween(fromDate, toDate);
        int currentlyOffline = basicStats.countCurrentlyOfflineDevices();
        List<LocationCount> locationCounts = locationStats.findEventsByLocationBetween(fromDate, toDate);
        List<TopRecoveryTimeEvent> topRecoveryTimeEvents = downtimeStats.findTopRecoveryTimeEvents(fromDate, toDate);
        List<WeekdayCount> weekdayCounts = basicStats.findEventCountByWeekday(fromDate, toDate);
        List<DowntimeBucket> downtimeHistogram = downtimeStats.getDowntimeHistogram(fromDate, toDate);
        List<TagTrend> tagTrends = tagStats.getTagTrends(fromDate, toDate);
        List<MostNotifiedDevice> mostNotified = notificationStats.findMostNotifiedDevices(fromDate, toDate);
        List<AverageRestoreByLocation> avgRestoreByLocation = locationStats.findAverageRestoreTimesPerLocation(fromDate, toDate);
        List<NotificationChannelCount> channelCounts = notificationStats.getNotificationChannelDistributionBetween(fromDate, toDate);      
        MissedCheckinSummary missedSummary = checkinStats.getMissedCheckinStatsBetween(fromDate, toDate);
        List<DeviceStability> stabilityList = deviceStats.computeDeviceStability(fromDate, toDate);
        List<String> silentDevices = deviceStats.findDevicesWithNoEventsBetween(fromDate, toDate);
        model.addAttribute("silentDevices", silentDevices);        

        // Add to model
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

        return "statistics";
    }
}
