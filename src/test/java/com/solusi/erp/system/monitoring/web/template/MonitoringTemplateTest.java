package com.solusi.erp.system.monitoring.web.template;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Monitoring Template — Static Checks")
class MonitoringTemplateTest {

    private static final String TEMPLATE_PATH = "templates/system/monitoring/index.html";

    private String readTemplate() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(TEMPLATE_PATH)) {
            assertThat(is).as("Template file must exist: " + TEMPLATE_PATH).isNotNull();
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    @DisplayName("Template contains health-cards-container fragment ID")
    void templateContainsHealthCardsFragment() throws IOException {
        String html = readTemplate();
        assertThat(html).contains("health-cards-container");
    }

    @Test
    @DisplayName("Template contains log-table-container fragment ID")
    void templateContainsLogTableFragment() throws IOException {
        String html = readTemplate();
        assertThat(html).contains("log-table-container");
    }

    @Test
    @DisplayName("Template references health DTO properties")
    void templateReferencesHealthProperties() throws IOException {
        String html = readTemplate();
        assertThat(html).contains("${health.jvm.uptime}");
        assertThat(html).contains("${health.jvm.usedMemoryMb}");
        assertThat(html).contains("${health.jvm.maxMemoryMb}");
        assertThat(html).contains("${health.jvm.memoryPercent}");
        assertThat(html).contains("${health.jvm.activeThreads}");
        assertThat(html).contains("${health.jvm.progressBarClass}");
    }

    @Test
    @DisplayName("Template references service health properties via th:each")
    void templateReferencesServiceHealthProperties() throws IOException {
        String html = readTemplate();
        assertThat(html).contains("${health.services}");
        assertThat(html).contains("svc.name");
        assertThat(html).contains("svc.status");
        assertThat(html).contains("svc.detail");
        assertThat(html).contains("svc.badgeClass");
    }

    @Test
    @DisplayName("Template references log entry properties via th:each")
    void templateReferencesLogEntryProperties() throws IOException {
        String html = readTemplate();
        assertThat(html).contains("${logs}");
        assertThat(html).contains("${entry.level}");
        assertThat(html).contains("${entry.timestamp}");
        assertThat(html).contains("${entry.message}");
        assertThat(html).contains("${entry.stackTrace");
    }

    @Test
    @DisplayName("Template has HTMX health refresh trigger")
    void templateHasHtmxHealthTrigger() throws IOException {
        String html = readTemplate();
        assertThat(html).contains("hx-get=\"/monitoring/health\"");
        assertThat(html).contains("hx-trigger=");
    }

    @Test
    @DisplayName("Template uses sec:authorize for download buttons")
    void templateUsesSecAuthorizeForDownload() throws IOException {
        String html = readTemplate();
        assertThat(html).contains("sec:authorize=\"hasAuthority('MONITORING_DOWNLOAD')\"");
    }

    @Test
    @DisplayName("Template has download links for error and all logs")
    void templateHasDownloadLinks() throws IOException {
        String html = readTemplate();
        assertThat(html).contains("/monitoring/download");
        assertThat(html).contains("errorOnly=true");
        assertThat(html).contains("errorOnly=false");
    }

    @Test
    @DisplayName("Template has level toggle chips")
    void templateHasLevelChips() throws IOException {
        String html = readTemplate();
        assertThat(html).contains("id=\"level-chips\"");
    }

    @Test
    @DisplayName("Template has time range button group")
    void templateHasTimeRangeGroup() throws IOException {
        String html = readTemplate();
        assertThat(html).contains("id=\"time-range-group\"");
    }

    @Test
    @DisplayName("Template has session filter dropdown")
    void templateHasSessionFilter() throws IOException {
        String html = readTemplate();
        assertThat(html).contains("id=\"session-filter\"");
    }

    @Test
    @DisplayName("Template has keyword search input")
    void templateHasKeywordSearch() throws IOException {
        String html = readTemplate();
        assertThat(html).contains("id=\"keyword-search\"");
    }
}
