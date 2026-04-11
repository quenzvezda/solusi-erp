package com.solusi.erp.accounting.coa.web.template.integration;

import com.solusi.erp.accounting.coa.domain.model.AccountType;
import com.solusi.erp.accounting.coa.web.dto.CoaSummaryResponse;
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
 * Integration test for COA list template — Thymeleaf rendering with SpringSecurityDialect.
 * Validates sec:authorize show/hide buttons per authority.
 */
@DisplayName("COA List — Template Integration Test (sec:authorize visibility)")
@Tag("integration-template")
class CoaListIntegrationTest {

    private static final String TEMPLATE = "accounting/coa/list";

    // ── helpers ──────────────────────────────────────────────────────────────

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token =
                new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private Map<String, Object> modelWithEmptyPage() {
        var page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        return Map.of("page", page, "keyword", "",
                "accountTypes", AccountType.values());
    }

    private Map<String, Object> modelWithOneCoa() {
        CoaSummaryResponse dto = TestDtoFactory.sampleCoaSummaryResponse();
        var page = new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
        return Map.of("page", page, "keyword", "",
                "accountTypes", AccountType.values());
    }

    // ── baseline render ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Template renders without error for user with ACCOUNTING-COA_READ")
    void withCoaRead_templateRendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("ACCOUNTING-COA_READ"));

        assertThat(html).isNotBlank();
        assertThat(html).contains("coa-table-container");
    }

    // ── Add button (ACCOUNTING-COA_CREATE) ──────────────────────────────────

    @Test
    @DisplayName("sec:authorize — ACCOUNTING-COA_CREATE shows Add button")
    void withCoaCreate_addButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(),
                auth("ACCOUNTING-COA_READ", "ACCOUNTING-COA_CREATE"));

        assertThat(html).contains("/accounting/coa/create");
    }

    @Test
    @DisplayName("sec:authorize — missing ACCOUNTING-COA_CREATE hides Add button")
    void withoutCoaCreate_addButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("ACCOUNTING-COA_READ"));

        assertThat(html).doesNotContain("/accounting/coa/create");
    }

    // ── Edit button (ACCOUNTING-COA_UPDATE) ─────────────────────────────────

    @Test
    @DisplayName("sec:authorize — ACCOUNTING-COA_UPDATE shows Edit button")
    void withCoaUpdate_editButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneCoa(),
                auth("ACCOUNTING-COA_READ", "ACCOUNTING-COA_UPDATE"));

        assertThat(html).contains("Cash");
        assertThat(html).contains("/accounting/coa/edit");
    }

    @Test
    @DisplayName("sec:authorize — missing ACCOUNTING-COA_UPDATE hides Edit button")
    void withoutCoaUpdate_editButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneCoa(), auth("ACCOUNTING-COA_READ"));

        assertThat(html).contains("Cash");
        assertThat(html).doesNotContain("/accounting/coa/edit");
    }

    // ── Delete button (ACCOUNTING-COA_DELETE) ───────────────────────────────

    @Test
    @DisplayName("sec:authorize — ACCOUNTING-COA_DELETE shows Delete button")
    void withCoaDelete_deleteButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneCoa(),
                auth("ACCOUNTING-COA_READ", "ACCOUNTING-COA_DELETE"));

        assertThat(html).contains("Cash");
        assertThat(html).contains("modal-delete-1");
    }

    @Test
    @DisplayName("sec:authorize — missing ACCOUNTING-COA_DELETE hides Delete button")
    void withoutCoaDelete_deleteButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneCoa(), auth("ACCOUNTING-COA_READ"));

        assertThat(html).contains("Cash");
        assertThat(html).doesNotContain("modal-delete-1");
    }
}
