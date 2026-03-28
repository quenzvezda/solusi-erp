package com.solusi.erp.master.currency.web.template.integration;

import com.solusi.erp.master.currency.web.dto.CurrencySummaryResponse;
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

@DisplayName("Currency List — Template Integration Test (sec:authorize visibility)")
@Tag("integration-template")
class CurrencyListIntegrationTest {

    private static final String TEMPLATE = "master/currency/list";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token =
                new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private Map<String, Object> modelWithEmptyPage() {
        org.springframework.data.domain.Page<CurrencySummaryResponse> emptyPage =
                new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        return Map.of("page", emptyPage, "keyword", "");
    }

    private Map<String, Object> modelWithOneCurrency() {
        CurrencySummaryResponse dto = new CurrencySummaryResponse();
        dto.setId(1L);
        dto.setSymbol("$");
        dto.setAlias("USD");
        dto.setName("US Dollar");
        dto.setIsDefault(false);
        dto.setIsActive(true);

        org.springframework.data.domain.Page<CurrencySummaryResponse> page =
                new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
        return Map.of("page", page, "keyword", "");
    }

    @Test
    @DisplayName("Template renders without error for user with CURRENCY_READ")
    void withCurrencyRead_templateRendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("CURRENCY_READ"));

        assertThat(html).isNotBlank();
        assertThat(html).contains("currency-table-container");
    }

    @Test
    @DisplayName("sec:authorize — CURRENCY_CREATE shows Add New Currency button")
    void withCurrencyCreate_addButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("CURRENCY_READ", "CURRENCY_CREATE"));

        assertThat(html).contains("/master/currencies/create");
    }

    @Test
    @DisplayName("sec:authorize — missing CURRENCY_CREATE hides Add New Currency button")
    void withoutCurrencyCreate_addButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("CURRENCY_READ"));

        assertThat(html).doesNotContain("/master/currencies/create");
    }

    @Test
    @DisplayName("sec:authorize — CURRENCY_UPDATE shows Edit button for each row")
    void withCurrencyUpdate_editButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneCurrency(), auth("CURRENCY_READ", "CURRENCY_UPDATE"));

        assertThat(html).contains("US Dollar");
        assertThat(html).contains("/master/currencies/edit");
    }

    @Test
    @DisplayName("sec:authorize — CURRENCY_DELETE shows Delete button for each row")
    void withCurrencyDelete_deleteButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneCurrency(), auth("CURRENCY_READ", "CURRENCY_DELETE"));

        assertThat(html).contains("US Dollar");
        assertThat(html).contains("modal-delete-1");
    }
}
