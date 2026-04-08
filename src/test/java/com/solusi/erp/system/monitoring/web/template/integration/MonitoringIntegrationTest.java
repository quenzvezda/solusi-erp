package com.solusi.erp.system.monitoring.web.template.integration;

import com.solusi.erp.system.monitoring.domain.model.LogEntry;
import com.solusi.erp.system.monitoring.domain.model.ServerSession;
import com.solusi.erp.system.monitoring.web.dto.MonitoringHealthResponse;
import com.solusi.erp.system.monitoring.domain.model.ServiceHealth;
import com.solusi.erp.system.monitoring.domain.model.SystemHealthSnapshot;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Monitoring page template — Thymeleaf rendering with SpringSecurityDialect.
 */
@DisplayName("Monitoring — Template Integration Test (sec:authorize visibility)")
@Tag("integration-template")
class MonitoringIntegrationTest {

    private static final String TEMPLATE = "system/monitoring/index";

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token =
                new TestingAuthenticationToken("admin", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private MonitoringHealthResponse sampleHealthResponse() {
        List<ServiceHealth> services = List.of(
                ServiceHealth.up("database", "Ping: 1ms"),
                ServiceHealth.up("minio", "Latency: 3ms"),
                ServiceHealth.up("disk", "50 GB free / 100 GB total (50% used)")
        );
        SystemHealthSnapshot.JvmMetrics jvm = new SystemHealthSnapshot.JvmMetrics("1h 0m", 128, 512, 25, 20);
        SystemHealthSnapshot snapshot = new SystemHealthSnapshot(services, jvm);
        return MonitoringHealthResponse.from(snapshot, "08 Apr 2026 10:00:00 WIB");
    }

    private Map<String, Object> baseModel() {
        Map<String, Object> model = new HashMap<>();
        model.put("health", sampleHealthResponse());
        model.put("currentTime", "08 Apr 2026 10:00:00 WIB");
        model.put("keyword", "");
        model.put("sessionId", null);
        model.put("timeRange", "1h");
        model.put("totalMatched", 0L);
        model.put("errorCount", 0L);
        model.put("warnCount", 0L);
        model.put("infoCount", 0L);
        model.put("debugCount", 0L);
        model.put("displayedCount", 0L);
        model.put("sessions", new ArrayList<ServerSession>());
        model.put("logs", new ArrayList<LogEntry>());
        return model;
    }

    private Map<String, Object> modelWithLogs() {
        Map<String, Object> model = baseModel();
        List<LogEntry> logs = new ArrayList<>(List.of(
                new LogEntry("2026-04-08 10:00:00", "INFO", "main", "c.s.e.App", "com.solusi.erp.App", "Application started", null),
                new LogEntry("2026-04-08 10:00:01", "ERROR", "main", "c.s.e.Svc", "com.solusi.erp.Svc", "NullPointer", "java.lang.NullPointerException\n\tat Main.java:1")
        ));
        model.put("logs", logs);
        model.put("totalMatched", 2L);
        model.put("errorCount", 1L);
        model.put("infoCount", 1L);
        model.put("displayedCount", 2L);
        return model;
    }

    // ─── 1. Baseline render ─────────────────────────────────────────────────

    @Test
    @DisplayName("Template renders without error for user with MONITORING_READ")
    void withMonitoringRead_templateRendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, baseModel(), auth("MONITORING_READ"));

        assertThat(html).isNotBlank();
        assertThat(html).contains("health-cards-container");
        assertThat(html).contains("log-table-container");
    }

    @Test
    @DisplayName("Template renders health cards with service info")
    void templateRendersHealthCards() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithLogs(), auth("MONITORING_READ"));

        assertThat(html).contains("1h 0m");
        assertThat(html).contains("128");
        assertThat(html).contains("512");
    }

    @Test
    @DisplayName("Template renders log entries with data")
    void templateRendersLogEntries() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithLogs(), auth("MONITORING_READ"));

        assertThat(html).contains("Application started");
        assertThat(html).contains("NullPointer");
        assertThat(html).contains("ERROR");
        assertThat(html).contains("INFO");
    }

    @Test
    @DisplayName("Template shows stack trace details for error entries")
    void templateShowsStackTrace() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithLogs(), auth("MONITORING_READ"));

        assertThat(html).contains("java.lang.NullPointerException");
    }

    // ─── 2. sec:authorize — Download buttons (MONITORING_DOWNLOAD) ──────────

    @Test
    @DisplayName("sec:authorize — MONITORING_DOWNLOAD shows download buttons")
    void withMonitoringDownload_downloadButtonsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, baseModel(), auth("MONITORING_READ", "MONITORING_DOWNLOAD"));

        assertThat(html).contains("/monitoring/download");
    }

    @Test
    @DisplayName("sec:authorize — missing MONITORING_DOWNLOAD hides download buttons")
    void withoutMonitoringDownload_downloadButtonsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, baseModel(), auth("MONITORING_READ"));

        assertThat(html).doesNotContain("/monitoring/download");
    }

    // ─── 3. Empty state ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Template shows empty state when no log entries")
    void withNoLogs_showsEmptyMessage() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, baseModel(), auth("MONITORING_READ"));

        assertThat(html).contains("log-table-container");
    }
}
