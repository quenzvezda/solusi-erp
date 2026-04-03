package com.solusi.erp.master.bankaccount.web.template.integration;

import com.solusi.erp.master.bankaccount.web.dto.BankAccountSummaryResponse;
import com.solusi.erp.testutils.TemplateTestUtils;
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
 * Integration test for BankAccount list template — Thymeleaf rendering with SpringSecurityDialect.
 *
 * <p><strong>What is tested:</strong></p>
 * <ul>
 *   <li>Template renders without error for a valid model.</li>
 *   <li>{@code sec:authorize="hasAuthority('BANK-ACCOUNT_CREATE')"} — the Add-button div is shown
 *       when the user has {@code BANK-ACCOUNT_CREATE} and hidden when they do not.</li>
 *   <li>{@code sec:authorize="hasAuthority('BANK-ACCOUNT_UPDATE/DELETE')"} — per-row action buttons
 *       follow the same pattern.</li>
 * </ul>
 */
@DisplayName("BankAccount List — Template Integration Test (sec:authorize visibility)")
@Tag("integration-template")
class BankAccountListIntegrationTest {

    private static final String TEMPLATE = "master/bank-accounts/list";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token =
                new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private Map<String, Object> modelWithEmptyPage() {
        org.springframework.data.domain.Page<BankAccountSummaryResponse> emptyPage =
                new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        return Map.of("page", emptyPage, "keyword", "");
    }

    private Map<String, Object> modelWithOneBankAccount() {
        BankAccountSummaryResponse dto = new BankAccountSummaryResponse();
        dto.setId(1L);
        dto.setCode("BA-001");
        dto.setBankName("Bank BCA");
        dto.setAccountName("John Doe");
        dto.setAccountNo("1234567890");
        dto.setAccountType("BANK");
        dto.setPartyName("PT ABC");

        org.springframework.data.domain.Page<BankAccountSummaryResponse> page =
                new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
        return Map.of("page", page, "keyword", "");
    }

    @Test
    @DisplayName("Template renders without error for user with BANK-ACCOUNT_READ")
    void withBankAccountRead_templateRendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("BANK-ACCOUNT_READ"));

        assertThat(html).isNotBlank();
        assertThat(html).contains("bank-account-list-content");
    }

    @Test
    @DisplayName("sec:authorize — BANK-ACCOUNT_CREATE shows Add New Bank Account button")
    void withBankAccountCreate_addButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("BANK-ACCOUNT_READ", "BANK-ACCOUNT_CREATE"));

        assertThat(html).contains("/master/bank-accounts/create");
    }

    @Test
    @DisplayName("sec:authorize — missing BANK-ACCOUNT_CREATE hides Add New Bank Account button")
    void withoutBankAccountCreate_addButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("BANK-ACCOUNT_READ"));

        assertThat(html).doesNotContain("/master/bank-accounts/create");
    }

    @Test
    @DisplayName("sec:authorize — BANK-ACCOUNT_UPDATE shows Edit button for each row")
    void withBankAccountUpdate_editButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneBankAccount(), auth("BANK-ACCOUNT_READ", "BANK-ACCOUNT_UPDATE"));

        assertThat(html).contains("Bank BCA");
        assertThat(html).contains("/master/bank-accounts/edit");
    }

    @Test
    @DisplayName("sec:authorize — missing BANK-ACCOUNT_UPDATE hides Edit button")
    void withoutBankAccountUpdate_editButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneBankAccount(), auth("BANK-ACCOUNT_READ"));

        assertThat(html).contains("Bank BCA");
        assertThat(html).doesNotContain("/master/bank-accounts/edit");
    }

    @Test
    @DisplayName("sec:authorize — BANK-ACCOUNT_DELETE shows Delete button for each row")
    void withBankAccountDelete_deleteButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneBankAccount(), auth("BANK-ACCOUNT_READ", "BANK-ACCOUNT_DELETE"));

        assertThat(html).contains("Bank BCA");
        assertThat(html).contains("modal-delete-1");
    }

    @Test
    @DisplayName("sec:authorize — missing BANK-ACCOUNT_DELETE hides Delete button")
    void withoutBankAccountDelete_deleteButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneBankAccount(), auth("BANK-ACCOUNT_READ"));

        assertThat(html).contains("Bank BCA");
        assertThat(html).doesNotContain("modal-delete-1");
    }
}
