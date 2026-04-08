package com.solusi.erp.system.monitoring.web.controller;

import com.solusi.erp.system.monitoring.application.service.MonitoringQueryService;
import com.solusi.erp.system.monitoring.domain.model.LogViewResult;
import com.solusi.erp.system.monitoring.domain.model.ServerSession;
import com.solusi.erp.system.monitoring.domain.model.SystemHealthSnapshot;
import com.solusi.erp.system.monitoring.web.dto.MonitoringHealthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for the System Monitoring page.
 * Provides health status, log viewing, and log download endpoints.
 */
@Controller
@RequestMapping("/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private static final ZoneId WIB = ZoneId.of("Asia/Jakarta");
    private static final DateTimeFormatter WIB_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss 'WIB'");

    private final MonitoringQueryService monitoringQueryService;

    @GetMapping
    @PreAuthorize("hasAuthority('MONITORING_READ')")
    public String index(Model model) {
        SystemHealthSnapshot snapshot = monitoringQueryService.getSystemHealth();
        String now = ZonedDateTime.now(WIB).format(WIB_FORMATTER);
        MonitoringHealthResponse healthResponse = MonitoringHealthResponse.from(snapshot, now);
        model.addAttribute("health", healthResponse);
        model.addAttribute("currentTime", now);

        // Sessions for dropdown
        List<ServerSession> sessions = monitoringQueryService.getServerSessions();
        model.addAttribute("sessions", sessions);

        // Initial log load (last 1 hour, all levels)
        LogViewResult logResult = monitoringQueryService.getRecentLogs(100, Set.of(), null, null, "1h");
        populateLogModel(model, logResult, "", null, "1h");

        return "system/monitoring/index";
    }

    @GetMapping("/logs")
    @PreAuthorize("hasAuthority('MONITORING_READ')")
    public String logs(
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) String levels,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer sessionId,
            @RequestParam(required = false) String timeRange,
            Model model) {
        Set<String> levelSet = parseLevels(levels);
        LogViewResult result = monitoringQueryService.getRecentLogs(limit, levelSet, keyword, sessionId, timeRange);
        populateLogModel(model, result, keyword, sessionId, timeRange);
        return "system/monitoring/index :: log-table-container";
    }

    @GetMapping("/health")
    @PreAuthorize("hasAuthority('MONITORING_READ')")
    public String healthFragment(Model model) {
        SystemHealthSnapshot snapshot = monitoringQueryService.getSystemHealth();
        String now = ZonedDateTime.now(WIB).format(WIB_FORMATTER);
        MonitoringHealthResponse healthResponse = MonitoringHealthResponse.from(snapshot, now);
        model.addAttribute("health", healthResponse);
        model.addAttribute("currentTime", now);
        return "system/monitoring/index :: health-cards-container";
    }

    @GetMapping("/api/health")
    @PreAuthorize("hasAuthority('MONITORING_READ')")
    @ResponseBody
    public ResponseEntity<MonitoringHealthResponse> healthApi() {
        SystemHealthSnapshot snapshot = monitoringQueryService.getSystemHealth();
        String now = ZonedDateTime.now(WIB).format(WIB_FORMATTER);
        return ResponseEntity.ok(MonitoringHealthResponse.from(snapshot, now));
    }

    @GetMapping("/download")
    @PreAuthorize("hasAuthority('MONITORING_DOWNLOAD')")
    public ResponseEntity<InputStreamResource> downloadLogs(
            @RequestParam(defaultValue = "false") boolean errorOnly) {
        InputStream stream = monitoringQueryService.getLogFileStream(errorOnly);
        long size = monitoringQueryService.getLogFileSize(errorOnly);
        String filename = errorOnly ? "erp-error.log" : "erp.log";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(size)
                .body(new InputStreamResource(stream));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void populateLogModel(Model model, LogViewResult result,
                                   String keyword, Integer sessionId, String timeRange) {
        model.addAttribute("logs", new ArrayList<>(result.entries()));
        model.addAttribute("totalMatched", result.totalMatched());
        model.addAttribute("errorCount", result.errorCount());
        model.addAttribute("warnCount", result.warnCount());
        model.addAttribute("infoCount", result.infoCount());
        model.addAttribute("debugCount", result.debugCount());
        model.addAttribute("displayedCount", (long) result.entries().size());
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("timeRange", timeRange != null ? timeRange : "1h");
    }

    Set<String> parseLevels(String levels) {
        if (levels == null || levels.isBlank()) return Set.of();
        return Arrays.stream(levels.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
