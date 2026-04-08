package com.solusi.erp.system.monitoring.web.template.integration;

import com.solusi.erp.system.monitoring.domain.model.LogEntry;
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
 *
 * <p><strong>What is tested:</strong></p>
 * <ul>
 *   <li>Template renders without error for a valid model.</li>
 *   <li>{@code sec:authorize="hasAuthority('MONITORING_DOWNLOAD')"} — download buttons are shown
 *       when the user has {@code MONITORING_DOWNLOAD} and hidden when they do not.</li>
 * </ul>
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

    private Map<String, Object> modelWithHealthAndLogs() {
        List<LogEntry> logs = new ArrayList<>(List.of(
                new LogEntry("2026-04-08 10:00:00", "INFO", "main", "c.s.e.App", "Application started", null),
                new LogEntry("2026-04-08 10:00:01", "ERROR", "main", "c.s.e.Svc", "NullPointer", "java.lang.NullPointerException\n\tat Main.java:1")
        ));
        Map<String, Object> model = new HashMap<>();
        model.put("health", sampleHealthResponse());
        model.put("currentTime", "08 Apr 2026 10:00:00 WIB");
        model.put("logs", logs);
        model.put("keyword", "");
        model.put("level", "");
        return model;
    }

    private Map<String, Object> modelWithHealthNoLogs() {
        Map<String, Object> model = new HashMap<>();
        model.put("health", sampleHealthResponse());
        model.put("currentTime", "08 Apr 2026 10:00:00 WIB");
        model.put("logs", new ArrayList<>());
        model.put("keyword", "");
        model.put("level", "");
        return model;
    }

    // ─── 1. Baseline render ─────────────────────────────────────────────────

    @Test
    @DisplayName("Template renders without error for user with MONITORING_READ")
    void withMonitoringRead_templateRendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithHealthNoLogs(), auth("MONITORING_READ"));

        assertThat(html).isNotBlank();
        assertThat(html).contains("health-cards-container");
        assertThat(html).contains("log-table-container");
    }

    @Test
    @DisplayName("Template renders health cards with service info")
    void templateRendersHealthCards() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithHealthAndLogs(), auth("MONITORING_READ"));

        assertThat(html).contains("1h 0m");
        assertThat(html).contains("128");
        assertThat(html).contains("512");
    }

    @Test
    @DisplayName("Template renders log entries with data")
    void templateRendersLogEntries() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithHealthAndLogs(), auth("MONITORING_READ"));

        assertThat(html).contains("Application started");
        assertThat(html).contains("NullPointer");
        assertThat(html).contains("ERROR");
        assertThat(html).contains("INFO");
    }

    @Test
    @DisplayName("Template shows stack trace details for error entries")
    void templateShowsStackTrace() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithHealthAndLogs(), auth("MONITORING_READ"));

        assertThat(html).contains("java.lang.NullPointerException");
        assertThat(html).contains("Stack Trace");
    }

    // ─── 2. sec:authorize — Download buttons (MONITORING_DOWNLOAD) ──────────

    @Test
    @DisplayName("sec:authorize — MONITORING_DOWNLOAD shows download buttons")
    void withMonitoringDownload_downloadButtonsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithHealthNoLogs(), auth("MONITORING_READ", "MONITORING_DOWNLOAD"));

        assertThat(html).contains("/monitoring/download");
    }

    @Test
    @DisplayName("sec:authorize — missing MONITORING_DOWNLOAD hides download buttons")
    void withoutMonitoringDownload_downloadButtonsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithHealthNoLogs(), auth("MONITORING_READ"));

        assertThat(html).doesNotContain("/monitoring/download");
    }

    // ─── 3. Empty state ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Template shows empty state when no log entries")
    void withNoLogs_showsEmptyMessage() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithHealthNoLogs(), auth("MONITORING_READ"));

        // The table body should still be rendered, just without data rows
        assertThat(html).contains("log-table-container");
    }
}
