package com.solusi.erp.system.monitoring.web.controller;

import com.solusi.erp.system.monitoring.application.service.MonitoringQueryService;
import com.solusi.erp.system.monitoring.domain.model.LogEntry;
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
import java.util.List;

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
        return "system/monitoring/index";
    }

    @GetMapping("/logs")
    @PreAuthorize("hasAuthority('MONITORING_READ')")
    public String logs(
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String keyword,
            Model model) {
        List<LogEntry> logs = monitoringQueryService.getRecentLogs(limit, level, keyword);
        model.addAttribute("logs", logs);
        model.addAttribute("keyword", keyword);
        model.addAttribute("level", level);
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
}
