package com.solusi.erp.system.monitoring.web.controller;

import com.solusi.erp.system.monitoring.application.service.MonitoringQueryService;
import com.solusi.erp.system.monitoring.domain.model.LogEntry;
import com.solusi.erp.system.monitoring.domain.model.ServiceHealth;
import com.solusi.erp.system.monitoring.domain.model.SystemHealthSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("MonitoringController — Unit Test")
public class MonitoringControllerTest {

    private MonitoringQueryService monitoringQueryService;
    private MonitoringController controller;

    @BeforeEach
    void setUp() {
        monitoringQueryService = mock(MonitoringQueryService.class);
        controller = new MonitoringController(monitoringQueryService);
    }

    private SystemHealthSnapshot sampleSnapshot() {
        List<ServiceHealth> services = List.of(
                ServiceHealth.up("database", "Ping: 1ms"),
                ServiceHealth.up("minio", "Latency: 3ms"),
                ServiceHealth.up("disk", "50 GB free / 100 GB total (50% used)")
        );
        SystemHealthSnapshot.JvmMetrics jvm = new SystemHealthSnapshot.JvmMetrics("1h 0m", 128, 512, 25, 20);
        return new SystemHealthSnapshot(services, jvm);
    }

    @Test
    @DisplayName("index returns correct view name and populates health model")
    void index_returnsCorrectViewAndModel() {
        when(monitoringQueryService.getSystemHealth()).thenReturn(sampleSnapshot());

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.index(model);

        assertThat(view).isEqualTo("system/monitoring/index");
        assertThat(model.get("health")).isNotNull();
        assertThat(model.get("currentTime")).isNotNull();
        assertThat(model.get("currentTime").toString()).contains("WIB");
    }

    @Test
    @DisplayName("logs returns fragment name and populates log entries model")
    void logs_returnsFragmentAndModel() {
        List<LogEntry> entries = List.of(
                new LogEntry("2026-04-08 10:00:00", "INFO", "main", "c.s.e.App", "Started", null)
        );
        when(monitoringQueryService.getRecentLogs(100, "INFO", "start")).thenReturn(entries);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.logs(100, "INFO", "start", model);

        assertThat(view).isEqualTo("system/monitoring/index :: log-table-container");
        assertThat(model.get("logs")).isEqualTo(entries);
        assertThat(model.get("keyword")).isEqualTo("start");
        assertThat(model.get("level")).isEqualTo("INFO");
    }

    @Test
    @DisplayName("healthFragment returns fragment name with health model")
    void healthFragment_returnsFragmentAndModel() {
        when(monitoringQueryService.getSystemHealth()).thenReturn(sampleSnapshot());

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.healthFragment(model);

        assertThat(view).isEqualTo("system/monitoring/index :: health-cards-container");
        assertThat(model.get("health")).isNotNull();
        assertThat(model.get("currentTime")).isNotNull();
    }

    @Test
    @DisplayName("downloadLogs returns response with correct headers for all logs")
    void downloadLogs_returnsResponseForAllLogs() {
        byte[] content = "log data".getBytes();
        when(monitoringQueryService.getLogFileStream(false)).thenReturn(new ByteArrayInputStream(content));
        when(monitoringQueryService.getLogFileSize(false)).thenReturn((long) content.length);

        var response = controller.downloadLogs(false);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo("erp.log");
    }

    @Test
    @DisplayName("downloadLogs returns error log file when errorOnly=true")
    void downloadLogs_returnsErrorLogWhenErrorOnly() {
        byte[] content = "error data".getBytes();
        when(monitoringQueryService.getLogFileStream(true)).thenReturn(new ByteArrayInputStream(content));
        when(monitoringQueryService.getLogFileSize(true)).thenReturn((long) content.length);

        var response = controller.downloadLogs(true);

        assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo("erp-error.log");
    }
}
