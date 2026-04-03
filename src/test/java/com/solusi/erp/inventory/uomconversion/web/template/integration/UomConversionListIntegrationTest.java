package com.solusi.erp.inventory.uomconversion.web.template.integration;

import com.solusi.erp.inventory.uomconversion.web.dto.UomConversionSummaryResponse;
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

/**
 * Integration test for UomConversion list template — Thymeleaf rendering with SpringSecurityDialect.
 */
@DisplayName("UomConversion List — Template Integration Test (sec:authorize visibility)")
@Tag("integration-template")
class UomConversionListIntegrationTest {

    private static final String TEMPLATE = "inventory/uom-conversions/list";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token =
            new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private Map<String, Object> modelWithEmptyPage() {
        org.springframework.data.domain.Page<UomConversionSummaryResponse> emptyPage =
            new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        return Map.of("page", emptyPage, "keyword", "");
    }

    private Map<String, Object> modelWithOneItem() {
        UomConversionSummaryResponse dto = new UomConversionSummaryResponse();
        dto.setId(1L);
        dto.setProductName("Product A");
        dto.setProductCode("PRD-001");
        dto.setFromUomName("Box");
        dto.setToUomName("Pieces");
        dto.setConversionFactor(new BigDecimal("12.00"));

        org.springframework.data.domain.Page<UomConversionSummaryResponse> page =
            new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
        return Map.of("page", page, "keyword", "");
    }

    @Test
    @DisplayName("Template renders without error for user with UOM-CONVERSION_READ")
    void withReadAuthority_templateRendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
            TEMPLATE, modelWithEmptyPage(), auth("UOM-CONVERSION_READ"));

        assertThat(html).isNotBlank();
        assertThat(html).contains("uom-conversion-table-container");
    }

    @Test
    @DisplayName("sec:authorize — UOM-CONVERSION_CREATE shows Add button")
    void withCreateAuthority_addButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
            TEMPLATE, modelWithEmptyPage(), auth("UOM-CONVERSION_READ", "UOM-CONVERSION_CREATE"));

        assertThat(html).contains("/inventory/uom-conversions/create");
    }

    @Test
    @DisplayName("sec:authorize — missing UOM-CONVERSION_CREATE hides Add button")
    void withoutCreateAuthority_addButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
            TEMPLATE, modelWithEmptyPage(), auth("UOM-CONVERSION_READ"));

        assertThat(html).doesNotContain("/inventory/uom-conversions/create");
    }

    @Test
    @DisplayName("sec:authorize — UOM-CONVERSION_UPDATE shows Edit button")
    void withUpdateAuthority_editButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
            TEMPLATE, modelWithOneItem(), auth("UOM-CONVERSION_READ", "UOM-CONVERSION_UPDATE"));

        assertThat(html).contains("Product A");
        assertThat(html).contains("/inventory/uom-conversions/edit");
    }

    @Test
    @DisplayName("sec:authorize — missing UOM-CONVERSION_UPDATE hides Edit button")
    void withoutUpdateAuthority_editButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
            TEMPLATE, modelWithOneItem(), auth("UOM-CONVERSION_READ"));

        assertThat(html).contains("Product A");
        assertThat(html).doesNotContain("/inventory/uom-conversions/edit");
    }

    @Test
    @DisplayName("sec:authorize — UOM-CONVERSION_DELETE shows Delete button")
    void withDeleteAuthority_deleteButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
            TEMPLATE, modelWithOneItem(), auth("UOM-CONVERSION_READ", "UOM-CONVERSION_DELETE"));

        assertThat(html).contains("Product A");
        assertThat(html).contains("modal-delete-1");
    }

    @Test
    @DisplayName("sec:authorize — missing UOM-CONVERSION_DELETE hides Delete button")
    void withoutDeleteAuthority_deleteButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
            TEMPLATE, modelWithOneItem(), auth("UOM-CONVERSION_READ"));

        assertThat(html).contains("Product A");
        assertThat(html).doesNotContain("modal-delete-1");
    }
}
