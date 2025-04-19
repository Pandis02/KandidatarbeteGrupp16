package kg16.demo.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import kg16.demo.model.dto.*;
import kg16.demo.model.services.StatisticsService;
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

    private final StatisticsService statisticsService;

    public ViewStatistics(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
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
        List<DayCount> dailyEvents = statisticsService.findEventsByDayBetween(fromDate, toDate);
        List<HourCount> hourlyEvents = statisticsService.findEventsByHourBetween(fromDate, toDate);
        List<TagCount> tagCounts = statisticsService.findCommonTagsBetween(fromDate, toDate);
        List<TopDevice> topDevices = statisticsService.findMostProblematicDevicesBetween(fromDate, toDate);
        int totalEventCount = statisticsService.countEventsBetween(fromDate, toDate);
        OptionalDouble avgDowntime = statisticsService.findAverageDowntimeBetween(fromDate, toDate);
        int currentlyOffline = statisticsService.countCurrentlyOfflineDevices();
        List<LocationCount> locationCounts = statisticsService.findEventsByLocationBetween(fromDate, toDate);
        List<TopRecoveryTimeEvent> topRecoveryTimeEvents = statisticsService.findTopRecoveryTimeEvents(fromDate, toDate);
        List<WeekdayCount> weekdayCounts = statisticsService.findEventCountByWeekday(fromDate, toDate);
        List<DowntimeBucket> downtimeHistogram = statisticsService.getDowntimeHistogram(fromDate, toDate);
        List<TagTrend> tagTrends = statisticsService.getTagTrends(fromDate, toDate);
        List<MostNotifiedDevice> mostNotified = statisticsService.findMostNotifiedDevices(fromDate, toDate);
        List<AverageRestoreByLocation> avgRestoreByLocation = statisticsService.findAverageRestoreTimesPerLocation(fromDate, toDate);
        List<NotificationChannelCount> channelCounts = statisticsService.getNotificationChannelDistributionBetween(fromDate, toDate);      
        MissedCheckinSummary missedSummary = statisticsService.getMissedCheckinStatsBetween(fromDate, toDate);
        List<DeviceStability> stabilityList = statisticsService.computeDeviceStability(fromDate, toDate);
        List<String> silentDevices = statisticsService.findDevicesWithNoEventsBetween(fromDate, toDate);
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
