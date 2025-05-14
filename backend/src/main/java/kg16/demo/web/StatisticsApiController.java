package kg16.demo.web;

import kg16.demo.model.dto.DayCount;
import kg16.demo.model.dto.HourCount;
import kg16.demo.model.dto.TagCount;
import kg16.demo.model.dto.TopDevice;
import kg16.demo.model.services.BasicStatsService;
import kg16.demo.model.services.DowntimeStatsService;
import kg16.demo.model.services.TagStatsService;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * REST controller for exposing system statistics such as event frequency, tag counts,
 * and problematic devices over a given time range.
 *
 * All endpoints accept optional `from` and `to` parameters (ISO-8601 strings),
 * defaulting to the past 7 days if not provided.
 */
@RestController
@RequestMapping("/api")
public class StatisticsApiController {

    private final DowntimeStatsService downtimeStats;
    private final BasicStatsService basicStats;
    private final TagStatsService tagStats;

    public StatisticsApiController(
            final DowntimeStatsService downtimeStats,
            final BasicStatsService basicStats,
            final TagStatsService tagStats) {
        this.downtimeStats = downtimeStats;
        this.basicStats = basicStats;
        this.tagStats = tagStats;
    }

    /**
     * Returns a list of daily event counts between two dates.
     *
     * @param from optional ISO-8601 start date (defaults to 7 days ago)
     * @param to   optional ISO-8601 end date (defaults to now)
     * @return list of {@link DayCount} records
     */
    @GetMapping("/daily-events")
    public List<DayCount> getDailyEvents(
            @RequestParam(required = false) final String from,
            @RequestParam(required = false) final String to) {
        return basicStats.findEventsByDayBetween(parseOrDefault(from, -7), parseOrDefault(to, 0));
    }

    /**
     * Returns a list of hourly event counts between two dates.
     *
     * @param from optional ISO-8601 start date (defaults to 7 days ago)
     * @param to   optional ISO-8601 end date (defaults to now)
     * @return list of {@link HourCount} records
     */
    @GetMapping("/hourly-events")
    public List<HourCount> getHourlyEvents(
            @RequestParam(required = false) final String from,
            @RequestParam(required = false) final String to) {
        return basicStats.findEventsByHourBetween(parseOrDefault(from, -7), parseOrDefault(to, 0));
    }

    /**
     * Returns the most problematic devices (with highest event count and downtime) during a time range.
     *
     * @param from optional ISO-8601 start date (defaults to 7 days ago)
     * @param to   optional ISO-8601 end date (defaults to now)
     * @return list of {@link TopDevice} records
     */
    @GetMapping("/top-devices")
    public List<TopDevice> getTopDevices(
            @RequestParam(required = false) final String from,
            @RequestParam(required = false) final String to) {
        return downtimeStats.findMostProblematicDevicesBetween(parseOrDefault(from, -7), parseOrDefault(to, 0));
    }

    /**
     * Returns tag frequency counts for events within the specified time range.
     *
     * @param from optional ISO-8601 start date (defaults to 7 days ago)
     * @param to   optional ISO-8601 end date (defaults to now)
     * @return list of {@link TagCount} records
     */
    @GetMapping("/tag-counts")
    public List<TagCount> getTagCounts(
            @RequestParam(required = false) final String from,
            @RequestParam(required = false) final String to) {
        return tagStats.findCommonTagsBetween(parseOrDefault(from, -7), parseOrDefault(to, 0));
    }

    // === Helpers ===

    /**
     * Parses an input string into {@link LocalDateTime} or applies an offset from now if null/blank.
     *
     * @param input       the input string (ISO-8601 format expected)
     * @param offsetDays  fallback offset in days if input is not provided
     * @return parsed or computed {@link LocalDateTime}
     */
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
