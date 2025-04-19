package kg16.demo.web;

import kg16.demo.model.dto.DayCount;
import kg16.demo.model.dto.HourCount;
import kg16.demo.model.dto.TagCount;
import kg16.demo.model.dto.TopDevice;
import kg16.demo.model.services.StatisticsService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class StatisticsApiController {

    private final StatisticsService statisticsService;

    public StatisticsApiController(final StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/daily-events")
    public List<DayCount> getDailyEvents(
            @RequestParam(required = false) final String from,
            @RequestParam(required = false) final String to) {
        return statisticsService.findEventsByDayBetween(parseOrDefault(from, -7), parseOrDefault(to, 0));
    }

    @GetMapping("/hourly-events")
    public List<HourCount> getHourlyEvents(
            @RequestParam(required = false) final String from,
            @RequestParam(required = false) final String to) {
        return statisticsService.findEventsByHourBetween(parseOrDefault(from, -7), parseOrDefault(to, 0));
    }

    @GetMapping("/top-devices")
    public List<TopDevice> getTopDevices(
            @RequestParam(required = false) final String from,
            @RequestParam(required = false) final String to) {
        return statisticsService.findMostProblematicDevicesBetween(parseOrDefault(from, -7), parseOrDefault(to, 0));
    }

    @GetMapping("/tag-counts")
    public List<TagCount> getTagCounts(
            @RequestParam(required = false) final String from,
            @RequestParam(required = false) final String to) {
        return statisticsService.findCommonTagsBetween(parseOrDefault(from, -7), parseOrDefault(to, 0));
    }

    // === Helpers ===

    private LocalDateTime parseOrDefault(String input, int offsetDays) {
        if (input == null || input.isBlank()) {
            return LocalDateTime.now().plusDays(offsetDays);
        }
        try {
            return LocalDateTime.parse(input);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected ISO-8601 (e.g. '2025-04-16T00:00:00')", e);
        }
    }
}
