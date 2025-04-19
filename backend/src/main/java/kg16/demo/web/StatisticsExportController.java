package kg16.demo.web;

import jakarta.servlet.http.HttpServletResponse;
import kg16.demo.model.services.StatisticsService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/export")
public class StatisticsExportController {

    private final StatisticsService statisticsService;

    public StatisticsExportController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping(value = "/tags", produces = "text/csv")
    public void exportTags(@RequestParam(required = false) String from,
                           @RequestParam(required = false) String to,
                           HttpServletResponse response) throws IOException {
        exportCsv(response, "tag_stats.csv", "Tag,Count",
                statisticsService.findCommonTagsBetween(parseFrom(from), parseTo(to)),
                t -> "%s,%d".formatted(t.tag(), t.count()));
    }

    @GetMapping(value = "/top-devices", produces = "text/csv")
    public void exportTopDevices(@RequestParam(required = false) String from,
                                 @RequestParam(required = false) String to,
                                 HttpServletResponse response) throws IOException {
        exportCsv(response, "top_devices.csv", "MAC Address,Events,Downtime",
                statisticsService.findMostProblematicDevicesBetween(parseFrom(from), parseTo(to)),
                d -> "%s,%d,%s".formatted(d.macAddress(), d.eventCount(), d.formattedDowntime()));
    }

    @GetMapping(value = "/weekday", produces = "text/csv")
    public void exportWeekday(@RequestParam(required = false) String from,
                              @RequestParam(required = false) String to,
                              HttpServletResponse response) throws IOException {
        exportCsv(response, "weekday_stats.csv", "Weekday,Count",
                statisticsService.findEventCountByWeekday(parseFrom(from), parseTo(to)),
                w -> "%s,%d".formatted(w.weekday(), w.count()));
    }

    @GetMapping(value = "/downtime", produces = "text/csv")
    public void exportDowntimeHistogram(@RequestParam(required = false) String from,
                                        @RequestParam(required = false) String to,
                                        HttpServletResponse response) throws IOException {
        exportCsv(response, "downtime_histogram.csv", "Range,Count",
                statisticsService.getDowntimeHistogram(parseFrom(from), parseTo(to)),
                d -> "%s,%d".formatted(d.rangeLabel(), d.count()));
    }

    @GetMapping(value = "/locations", produces = "text/csv")
    public void exportLocations(@RequestParam(required = false) String from,
                                @RequestParam(required = false) String to,
                                HttpServletResponse response) throws IOException {
        exportCsv(response, "location_stats.csv", "Location,Count",
                statisticsService.findEventsByLocationBetween(parseFrom(from), parseTo(to)),
                l -> "%s,%d".formatted(l.location(), l.count()));
    }

    @GetMapping(value = "/top-recovery", produces = "text/csv")
    public void exportTopRecovery(@RequestParam(required = false) String from,
                                  @RequestParam(required = false) String to,
                                  HttpServletResponse response) throws IOException {
        exportCsv(response, "top_recovery.csv", "MAC Address,Recovery Time (s),Recovered At",
                statisticsService.findTopRecoveryTimeEvents(parseFrom(from), parseTo(to)),
                e -> "%s,%d,%s".formatted(e.getMacAddress(), e.getRecoveryTimeSeconds(), e.getRestoredAtFormatted()));
    }

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
                    statisticsService.findCommonTagsBetween(fromDate, toDate),
                    t -> "%s,%d".formatted(t.tag(), t.count()));

            addZipEntry(zipOut, "top_devices.csv", "MAC Address,Events,Downtime",
                    statisticsService.findMostProblematicDevicesBetween(fromDate, toDate),
                    d -> "%s,%d,%s".formatted(d.macAddress(), d.eventCount(), d.formattedDowntime()));

            addZipEntry(zipOut, "weekday_stats.csv", "Weekday,Count",
                    statisticsService.findEventCountByWeekday(fromDate, toDate),
                    w -> "%s,%d".formatted(w.weekday(), w.count()));

            addZipEntry(zipOut, "downtime_histogram.csv", "Range,Count",
                    statisticsService.getDowntimeHistogram(fromDate, toDate),
                    d -> "%s,%d".formatted(d.rangeLabel(), d.count()));

            addZipEntry(zipOut, "location_stats.csv", "Location,Count",
                    statisticsService.findEventsByLocationBetween(fromDate, toDate),
                    l -> "%s,%d".formatted(l.location(), l.count()));

            addZipEntry(zipOut, "top_recovery.csv", "MAC Address,Recovery Time (s),Recovered At",
                    statisticsService.findTopRecoveryTimeEvents(fromDate, toDate),
                    e -> "%s,%d,%s".formatted(e.getMacAddress(), e.getRecoveryTimeSeconds(), e.getRestoredAtFormatted()));
        }
    }

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

    private <T> void addZipEntry(ZipOutputStream zipOut, String filename, String header,
                                  List<T> data, Function<T, String> formatter) throws IOException {
        zipOut.putNextEntry(new ZipEntry(filename));
        PrintWriter writer = new PrintWriter(zipOut);
        writer.println(header);
        for (T item : data) {
            writer.println(formatter.apply(item));
        }
        writer.flush();
        zipOut.closeEntry();
    }

    private LocalDateTime parseFrom(String input) {
        return (input == null || input.isEmpty())
                ? LocalDateTime.now().minusDays(7)
                : LocalDateTime.parse(input);
    }

    private LocalDateTime parseTo(String input) {
        return (input == null || input.isEmpty())
                ? LocalDateTime.now()
                : LocalDateTime.parse(input);
    }
}
