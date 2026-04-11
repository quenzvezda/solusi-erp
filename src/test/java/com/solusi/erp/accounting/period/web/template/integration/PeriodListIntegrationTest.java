package com.solusi.erp.accounting.period.web.template.integration;

import com.solusi.erp.accounting.period.web.dto.FiscalYearSummaryResponse;
import com.solusi.erp.testutils.TemplateTestUtils;
import com.solusi.erp.testutils.TestDtoFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Period list template — Thymeleaf rendering with SpringSecurityDialect.
 * Validates sec:authorize show/hide buttons per authority.
 */
@DisplayName("Period List — Template Integration Test (sec:authorize visibility)")
@Tag("integration-template")
class PeriodListIntegrationTest {

    private static final String TEMPLATE = "accounting/period/list";

    // ── helpers ──────────────────────────────────────────────────────────────

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token =
                new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private Map<String, Object> modelWithEmptyPage() {
        var page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        return Map.of("page", page, "keyword", "");
    }

    private Map<String, Object> modelWithOneFiscalYear() {
        FiscalYearSummaryResponse dto = TestDtoFactory.sampleFiscalYearSummaryResponse();
        var page = new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
        return Map.of("page", page, "keyword", "");
    }

    // ── baseline render ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Template renders without error for user with ACCOUNTING-PERIOD_READ")
    void withPeriodRead_templateRendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("ACCOUNTING-PERIOD_READ"));

        assertThat(html).isNotBlank();
        assertThat(html).contains("period-table-container");
    }

    // ── Add button (ACCOUNTING-PERIOD_CREATE) ───────────────────────────────

    @Test
    @DisplayName("sec:authorize — ACCOUNTING-PERIOD_CREATE shows Add button")
    void withPeriodCreate_addButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(),
                auth("ACCOUNTING-PERIOD_READ", "ACCOUNTING-PERIOD_CREATE"));

        assertThat(html).contains("/accounting/periods/create");
    }

    @Test
    @DisplayName("sec:authorize — missing ACCOUNTING-PERIOD_CREATE hides Add button")
    void withoutPeriodCreate_addButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("ACCOUNTING-PERIOD_READ"));

        assertThat(html).doesNotContain("/accounting/periods/create");
    }

    // ── Edit button (ACCOUNTING-PERIOD_UPDATE) ──────────────────────────────

    @Test
    @DisplayName("sec:authorize — ACCOUNTING-PERIOD_UPDATE shows Edit button")
    void withPeriodUpdate_editButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneFiscalYear(),
                auth("ACCOUNTING-PERIOD_READ", "ACCOUNTING-PERIOD_UPDATE"));

        assertThat(html).contains("Fiscal Year 2026");
        assertThat(html).contains("/accounting/periods/edit");
    }

    @Test
    @DisplayName("sec:authorize — missing ACCOUNTING-PERIOD_UPDATE hides Edit button")
    void withoutPeriodUpdate_editButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneFiscalYear(), auth("ACCOUNTING-PERIOD_READ"));

        assertThat(html).contains("Fiscal Year 2026");
        assertThat(html).doesNotContain("/accounting/periods/edit");
    }

    // ── Delete button (ACCOUNTING-PERIOD_DELETE) ────────────────────────────

    @Test
    @DisplayName("sec:authorize — ACCOUNTING-PERIOD_DELETE shows Delete button")
    void withPeriodDelete_deleteButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneFiscalYear(),
                auth("ACCOUNTING-PERIOD_READ", "ACCOUNTING-PERIOD_DELETE"));

        assertThat(html).contains("Fiscal Year 2026");
        assertThat(html).contains("modal-delete-1");
    }

    @Test
    @DisplayName("sec:authorize — missing ACCOUNTING-PERIOD_DELETE hides Delete button")
    void withoutPeriodDelete_deleteButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneFiscalYear(), auth("ACCOUNTING-PERIOD_READ"));

        assertThat(html).contains("Fiscal Year 2026");
        assertThat(html).doesNotContain("modal-delete-1");
    }
}
