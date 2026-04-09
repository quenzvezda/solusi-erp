package com.solusi.erp.system.monitoring.web.controller;

import com.solusi.erp.system.monitoring.application.service.MonitoringQueryService;
import com.solusi.erp.system.monitoring.domain.model.LogEntry;
import com.solusi.erp.system.monitoring.domain.model.LogViewResult;
import com.solusi.erp.system.monitoring.domain.model.ServerSession;
import com.solusi.erp.system.monitoring.domain.model.ServiceHealth;
import com.solusi.erp.system.monitoring.domain.model.SystemHealthSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
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

    private LogViewResult sampleLogResult() {
        List<LogEntry> entries = List.of(
                new LogEntry("2026-04-08 10:00:00", "INFO", "main", "c.s.e.App", "com.solusi.erp.App", "Started", null),
                new LogEntry("2026-04-08 10:00:01", "ERROR", "main", "c.s.e.App", "com.solusi.erp.App", "Failure", "NPE")
        );
        return new LogViewResult(entries, 2, 1, 0, 1, 0);
    }

    @Test
    @DisplayName("index returns correct view name and populates health and log model")
    void index_returnsCorrectViewAndModel() {
        when(monitoringQueryService.getSystemHealth()).thenReturn(sampleSnapshot());
        when(monitoringQueryService.getServerSessions()).thenReturn(List.of(
                new ServerSession(1, "2026-04-08 10:00:00", null, true, "Current Session")
        ));
        when(monitoringQueryService.getRecentLogs(anyInt(), anySet(), any(), any(), any(), any(), any()))
                .thenReturn(sampleLogResult());

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.index(model);

        assertThat(view).isEqualTo("system/monitoring/index");
        assertThat(model.get("health")).isNotNull();
        assertThat(model.get("currentTime")).isNotNull();
        assertThat(model.get("currentTime").toString()).contains("WIB");
        assertThat(model.get("sessions")).isNotNull();
        assertThat(model.get("logs")).isNotNull();
        assertThat(model.get("totalMatched")).isEqualTo(2L);
        assertThat(model.get("errorCount")).isEqualTo(1L);
        assertThat(model.get("timeRange")).isEqualTo("1h");
    }

    @Test
    @DisplayName("logs returns fragment name and populates log model with counts")
    void logs_returnsFragmentAndModel() {
        LogViewResult result = sampleLogResult();
        when(monitoringQueryService.getRecentLogs(100, Set.of("ERROR", "WARN"), "fail", null, "1h", null, null))
                .thenReturn(result);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.logs(100, "ERROR,WARN", "fail", null, "1h", null, null, model);

        assertThat(view).isEqualTo("system/monitoring/index :: log-table-container");
        assertThat(model.get("logs")).isNotNull();
        assertThat(model.get("keyword")).isEqualTo("fail");
        assertThat(model.get("totalMatched")).isEqualTo(2L);
        assertThat(model.get("errorCount")).isEqualTo(1L);
        assertThat(model.get("displayedCount")).isEqualTo(2L);
    }

    @Test
    @DisplayName("logs with empty levels passes empty set to service")
    void logs_emptyLevels_passesEmptySet() {
        when(monitoringQueryService.getRecentLogs(anyInt(), eq(Set.of()), any(), any(), any(), any(), any()))
                .thenReturn(new LogViewResult(List.of(), 0, 0, 0, 0, 0));

        ExtendedModelMap model = new ExtendedModelMap();
        controller.logs(100, "", null, null, null, null, null, model);

        verify(monitoringQueryService).getRecentLogs(100, Set.of(), null, null, null, null, null);
    }

    @Test
    @DisplayName("parseLevels handles comma-separated level string")
    void parseLevels_handlesCommaSeparated() {
        Set<String> result = controller.parseLevels("ERROR, WARN, INFO");
        assertThat(result).containsExactlyInAnyOrder("ERROR", "WARN", "INFO");
    }

    @Test
    @DisplayName("parseLevels handles null and blank input")
    void parseLevels_handlesNullAndBlank() {
        assertThat(controller.parseLevels(null)).isEmpty();
        assertThat(controller.parseLevels("")).isEmpty();
        assertThat(controller.parseLevels("   ")).isEmpty();
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
