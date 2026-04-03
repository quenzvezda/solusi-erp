package com.solusi.erp.master.tax.web.template.integration;

import com.solusi.erp.master.tax.web.dto.TaxSummaryResponse;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tax List — Template Integration Test (sec:authorize visibility)")
@Tag("integration-template")
class TaxListIntegrationTest {

    private static final String TEMPLATE = "master/tax/list";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token =
                new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private Map<String, Object> modelWithEmptyPage() {
        org.springframework.data.domain.Page<TaxSummaryResponse> emptyPage =
                new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        return Map.of("page", emptyPage, "keyword", "");
    }

    private Map<String, Object> modelWithOneTax() {
        TaxSummaryResponse dto = new TaxSummaryResponse();
        dto.setId(1L);
        dto.setCode("TX-01");
        dto.setName("PPN");
        dto.setRate(BigDecimal.valueOf(11));
        dto.setIsSubtract(false);
        dto.setIsActive(true);

        org.springframework.data.domain.Page<TaxSummaryResponse> page =
                new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
        return Map.of("page", page, "keyword", "");
    }

    @Test
    @DisplayName("Template renders without error for user with TAX_READ")
    void withTaxRead_templateRendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("TAX_READ"));

        assertThat(html).isNotBlank();
        assertThat(html).contains("tax-table-container");
    }

    @Test
    @DisplayName("sec:authorize — TAX_CREATE shows Add New Tax button")
    void withTaxCreate_addButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("TAX_READ", "TAX_CREATE"));

        assertThat(html).contains("/master/taxes/create");
    }

    @Test
    @DisplayName("sec:authorize — missing TAX_CREATE hides Add New Tax button")
    void withoutTaxCreate_addButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("TAX_READ"));

        assertThat(html).doesNotContain("/master/taxes/create");
    }

    @Test
    @DisplayName("sec:authorize — TAX_UPDATE shows Edit button for each row")
    void withTaxUpdate_editButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneTax(), auth("TAX_READ", "TAX_UPDATE"));

        assertThat(html).contains("PPN");
        assertThat(html).contains("/master/taxes/edit");
    }

    @Test
    @DisplayName("sec:authorize — missing TAX_UPDATE hides Edit button")
    void withoutTaxUpdate_editButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneTax(), auth("TAX_READ"));

        assertThat(html).contains("PPN");
        assertThat(html).doesNotContain("/master/taxes/edit");
    }

    @Test
    @DisplayName("sec:authorize — TAX_DELETE shows Delete button for each row")
    void withTaxDelete_deleteButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneTax(), auth("TAX_READ", "TAX_DELETE"));

        assertThat(html).contains("PPN");
        assertThat(html).contains("modal-delete-1");
    }

    @Test
    @DisplayName("sec:authorize — missing TAX_DELETE hides Delete button")
    void withoutTaxDelete_deleteButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneTax(), auth("TAX_READ"));

        assertThat(html).contains("PPN");
        assertThat(html).doesNotContain("modal-delete-1");
    }

}
