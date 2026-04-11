package com.solusi.erp.accounting.period.web.template.integration;

import com.solusi.erp.accounting.period.web.dto.FiscalYearDetailResponse;
import com.solusi.erp.accounting.period.web.dto.PeriodResponse;
import com.solusi.erp.testutils.TemplateTestUtils;
import com.solusi.erp.testutils.TestDtoFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Period detail template — Thymeleaf rendering with SpringSecurityDialect.
 * Validates sec:authorize visibility for period action buttons (Open/Close/Reopen).
 */
@DisplayName("Period Detail — Template Integration Test (sec:authorize visibility)")
@Tag("integration-template")
class PeriodDetailIntegrationTest {

    private static final String TEMPLATE = "accounting/period/detail";

    // ── helpers ──────────────────────────────────────────────────────────────

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token =
                new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private FiscalYearDetailResponse buildFyWithPeriodStatus(String status) {
        FiscalYearDetailResponse dto = new FiscalYearDetailResponse();
        dto.setId(1L);
        dto.setCode("FY-0001");
        dto.setName("Fiscal Year 2026");
        dto.setStartDate(java.time.LocalDate.of(2026, 1, 1));
        dto.setEndDate(java.time.LocalDate.of(2026, 12, 31));
        dto.setIsActive(true);
        dto.setPeriods(List.of(TestDtoFactory.samplePeriodResponse(10L, "FY-0001-01", "Jan 2026", 1, status)));
        return dto;
    }

    // ── baseline render ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Template renders without error and contains fiscal year data")
    void withPeriodRead_templateRendersSuccessfully() {
        FiscalYearDetailResponse fy = TestDtoFactory.sampleFiscalYearDetailResponse();
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, Map.of("fy", fy), auth("ACCOUNTING-PERIOD_READ"));

        assertThat(html).isNotBlank();
        assertThat(html).contains("FY-0001");
        assertThat(html).contains("Fiscal Year 2026");
    }

    // ── Close button for OPEN period ────────────────────────────────────────

    @Test
    @DisplayName("sec:authorize — ACCOUNTING-PERIOD_UPDATE shows Close button for OPEN period")
    void withPeriodUpdate_closeButtonVisibleForOpenPeriod() {
        FiscalYearDetailResponse fy = buildFyWithPeriodStatus("OPEN");
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, Map.of("fy", fy),
                auth("ACCOUNTING-PERIOD_READ", "ACCOUNTING-PERIOD_UPDATE"));

        assertThat(html).contains("/close");
    }

    // ── Reopen button for CLOSED period ─────────────────────────────────────

    @Test
    @DisplayName("sec:authorize — ACCOUNTING-PERIOD_UPDATE shows Reopen button for CLOSED period")
    void withPeriodUpdate_reopenButtonVisibleForClosedPeriod() {
        FiscalYearDetailResponse fy = buildFyWithPeriodStatus("CLOSED");
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, Map.of("fy", fy),
                auth("ACCOUNTING-PERIOD_READ", "ACCOUNTING-PERIOD_UPDATE"));

        assertThat(html).contains("/reopen");
    }

    // ── Open button for NEVER_OPENED period ─────────────────────────────────

    @Test
    @DisplayName("sec:authorize — ACCOUNTING-PERIOD_UPDATE shows Open button for NEVER_OPENED period")
    void withPeriodUpdate_openButtonVisibleForNeverOpenedPeriod() {
        FiscalYearDetailResponse fy = buildFyWithPeriodStatus("NEVER_OPENED");
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, Map.of("fy", fy),
                auth("ACCOUNTING-PERIOD_READ", "ACCOUNTING-PERIOD_UPDATE"));

        assertThat(html).contains("/reopen");
    }

    // ── Action buttons hidden without UPDATE ─────────────────────────────────

    @Test
    @DisplayName("sec:authorize — missing ACCOUNTING-PERIOD_UPDATE hides action buttons")
    void withoutPeriodUpdate_actionButtonsHidden() {
        FiscalYearDetailResponse fy = buildFyWithPeriodStatus("OPEN");
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, Map.of("fy", fy), auth("ACCOUNTING-PERIOD_READ"));

        assertThat(html).doesNotContain("/close");
        assertThat(html).doesNotContain("/reopen");
    }
}
