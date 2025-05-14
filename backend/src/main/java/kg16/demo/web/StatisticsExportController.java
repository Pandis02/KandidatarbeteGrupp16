package kg16.demo.web;

import jakarta.servlet.http.HttpServletResponse;
import kg16.demo.model.services.BasicStatsService;
import kg16.demo.model.services.DowntimeStatsService;
import kg16.demo.model.services.LocationStatsService;
import kg16.demo.model.services.TagStatsService;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * REST controller for exporting system statistics as downloadable CSV or ZIP files.
 * Provides endpoints to export tag data, location summaries, recovery stats, and more.
 */
@RestController
@RequestMapping("/api/export")
public class StatisticsExportController {

    private final BasicStatsService basicStats;
    private final TagStatsService tagStats;
    private final LocationStatsService locationStats;
    private final DowntimeStatsService downtimeStats;

    public StatisticsExportController(BasicStatsService basicStats, TagStatsService tagStats,
                                      LocationStatsService locationStats, DowntimeStatsService downtimeStats) {
        this.basicStats = basicStats;
        this.tagStats = tagStats;
        this.locationStats = locationStats;
        this.downtimeStats = downtimeStats;
    }

    /**
     * Exports tag frequency counts as a CSV file.
     */
    @GetMapping(value = "/tags", produces = "text/csv")
    public void exportTags(@RequestParam(required = false) String from,
                           @RequestParam(required = false) String to,
                           HttpServletResponse response) throws IOException {
        exportCsv(response, "tag_stats.csv", "Tag,Count",
                tagStats.findCommonTagsBetween(parseFrom(from), parseTo(to)),
                t -> "%s,%d".formatted(t.tag(), t.count()));
    }

    /**
     * Exports top problematic devices based on event count and downtime as a CSV file.
     */
    @GetMapping(value = "/top-devices", produces = "text/csv")
    public void exportTopDevices(@RequestParam(required = false) String from,
                                 @RequestParam(required = false) String to,
                                 HttpServletResponse response) throws IOException {
        exportCsv(response, "top_devices.csv", "MAC Address,Events,Downtime",
                downtimeStats.findMostProblematicDevicesBetween(parseFrom(from), parseTo(to)),
                d -> "%s,%d,%s".formatted(d.macAddress(), d.eventCount(), d.formattedDowntime()));
    }

    /**
     * Exports event counts grouped by weekday as a CSV file.
     */
    @GetMapping(value = "/weekday", produces = "text/csv")
    public void exportWeekday(@RequestParam(required = false) String from,
                              @RequestParam(required = false) String to,
                              HttpServletResponse response) throws IOException {
        exportCsv(response, "weekday_stats.csv", "Weekday,Count",
                basicStats.findEventCountByWeekday(parseFrom(from), parseTo(to)),
                w -> "%s,%d".formatted(w.weekday(), w.count()));
    }

    /**
     * Exports downtime distribution (buckets) as a CSV file.
     */
    @GetMapping(value = "/downtime", produces = "text/csv")
    public void exportDowntimeHistogram(@RequestParam(required = false) String from,
                                        @RequestParam(required = false) String to,
                                        HttpServletResponse response) throws IOException {
        exportCsv(response, "downtime_histogram.csv", "Range,Count",
                downtimeStats.getDowntimeHistogram(parseFrom(from), parseTo(to)),
                d -> "%s,%d".formatted(d.rangeLabel(), d.count()));
    }

    /**
     * Exports event counts grouped by location as a CSV file.
     */
    @GetMapping(value = "/locations", produces = "text/csv")
    public void exportLocations(@RequestParam(required = false) String from,
                                @RequestParam(required = false) String to,
                                HttpServletResponse response) throws IOException {
        exportCsv(response, "location_stats.csv", "Location,Count",
                locationStats.findEventsByLocationBetween(parseFrom(from), parseTo(to)),
                l -> "%s,%d".formatted(l.location(), l.count()));
    }

    /**
     * Exports the top 5 longest recovery time events as a CSV file.
     */
    @GetMapping(value = "/top-recovery", produces = "text/csv")
    public void exportTopRecovery(@RequestParam(required = false) String from,
                                  @RequestParam(required = false) String to,
                                  HttpServletResponse response) throws IOException {
        exportCsv(response, "top_recovery.csv", "MAC Address,Recovery Time (s),Recovered At",
                downtimeStats.findTopRecoveryTimeEvents(parseFrom(from), parseTo(to)),
                e -> "%s,%d,%s".formatted(e.getMacAddress(), e.getRecoveryTimeSeconds(), e.getRestoredAtFormatted()));
    }

    /**
     * Exports all statistics in a single ZIP file with multiple CSV entries inside.
     */
    @GetMapping(value = "/all", produces = "application/zip")
    public void exportAllAsZip(@RequestParam(required = false) String from,
                               @RequestParam(required = false) String to,
                               HttpServletResponse response) throws IOException {
        LocalDateTime fromDate = parseFrom(from);
        LocalDateTime toDate = parseTo(to);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Content-Disposition", "attachment; filename=statistics_export.zip");
        response.setContentType("application/zip");

        try (var zipOut = new ZipOutputStream(response.getOutputStream())) {
            addZipEntry(zipOut, "tag_stats.csv", "Tag,Count",
                    tagStats.findCommonTagsBetween(fromDate, toDate),
                    t -> "%s,%d".formatted(t.tag(), t.count()));

            addZipEntry(zipOut, "top_devices.csv", "MAC Address,Events,Downtime",
                    downtimeStats.findMostProblematicDevicesBetween(fromDate, toDate),
                    d -> "%s,%d,%s".formatted(d.macAddress(), d.eventCount(), d.formattedDowntime()));

            addZipEntry(zipOut, "weekday_stats.csv", "Weekday,Count",
                    basicStats.findEventCountByWeekday(fromDate, toDate),
                    w -> "%s,%d".formatted(w.weekday(), w.count()));

            addZipEntry(zipOut, "downtime_histogram.csv", "Range,Count",
                    downtimeStats.getDowntimeHistogram(fromDate, toDate),
                    d -> "%s,%d".formatted(d.rangeLabel(), d.count()));

            addZipEntry(zipOut, "location_stats.csv", "Location,Count",
                    locationStats.findEventsByLocationBetween(fromDate, toDate),
                    l -> "%s,%d".formatted(l.location(), l.count()));

            addZipEntry(zipOut, "top_recovery.csv", "MAC Address,Recovery Time (s),Recovered At",
                    downtimeStats.findTopRecoveryTimeEvents(fromDate, toDate),
                    e -> "%s,%d,%s".formatted(e.getMacAddress(), e.getRecoveryTimeSeconds(), e.getRestoredAtFormatted()));
        }
    }

    /**
     * Generic helper for writing a single CSV response to the HTTP stream.
     *
     * @param response  the servlet response
     * @param filename  file name to use in headers
     * @param header    CSV header line
     * @param data      data rows
     * @param formatter lambda to convert each data row to CSV string
     * @param <T>       type of data element
     */
    private <T> void exportCsv(HttpServletResponse response, String filename, String header,
                               List<T> data, Function<T, String> formatter) throws IOException {
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        response.setContentType("text/csv");

        try (PrintWriter writer = response.getWriter()) {
            writer.println(header);
            for (T item : data) {
                writer.println(formatter.apply(item));
            }
        }
    }

    /**
     * Helper for writing a CSV entry inside a ZIP archive.
     *
     * @param zipOut    the output stream
     * @param filename  the CSV filename within the ZIP
     * @param header    CSV header line
     * @param data      list of records
     * @param formatter lambda to convert data items to lines
     * @param <T>       type of record
     */
    private <T> void addZipEntry(ZipOutputStream zipOut, String filename, String header,
                                 List<T> data, Function<T, String> formatter) throws IOException {
        zipOut.putNextEntry(new ZipEntry(filename));
        PrintWriter writer = new PrintWriter(zipOut);
        writer.println(header);
        for (T item : data) {
            writer.println(formatter.apply(item));
        }
        writer.flush(); // Important for ZIP stream
        zipOut.closeEntry();
    }

    /**
     * Parses a `from` date string or defaults to 7 days ago.
     */
    private LocalDateTime parseFrom(String input) {
        return (input == null || input.isEmpty())
                ? LocalDateTime.now().minusDays(7)
                : LocalDateTime.parse(input);
    }

    /**
     * Parses a `to` date string or defaults to now.
     */
    private LocalDateTime parseTo(String input) {
        return (input == null || input.isEmpty())
                ? LocalDateTime.now()
                : LocalDateTime.parse(input);
    }
}
