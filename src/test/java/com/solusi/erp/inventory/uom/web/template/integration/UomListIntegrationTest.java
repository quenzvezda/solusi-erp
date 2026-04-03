package com.solusi.erp.inventory.uom.web.template.integration;

import com.solusi.erp.inventory.uom.web.dto.UomSummaryResponse;
import com.solusi.erp.inventory.uom.domain.model.UomType;
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
 * Integration test for UnitOfMeasure list template — Thymeleaf rendering with SpringSecurityDialect.
 */
@DisplayName("UOM List — Template Integration Test (sec:authorize visibility)")
@Tag("integration-template")
class UomListIntegrationTest {

    private static final String TEMPLATE = "inventory/unit-of-measures/list";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token =
            new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private Map<String, Object> modelWithEmptyPage() {
        org.springframework.data.domain.Page<UomSummaryResponse> emptyPage =
            new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        return Map.of("page", emptyPage, "keyword", "");
    }

    private Map<String, Object> modelWithOneUom() {
        UomSummaryResponse dto = new UomSummaryResponse();
        dto.setId(1L);
        dto.setCode("KG");
        dto.setName("Kilogram");
        dto.setType(UomType.WEIGHT);

        org.springframework.data.domain.Page<UomSummaryResponse> page =
            new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
        return Map.of("page", page, "keyword", "");
    }

    @Test
    @DisplayName("Template renders without error for user with UNIT-OF-MEASURE_READ")
    void withUomRead_templateRendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
            TEMPLATE, modelWithEmptyPage(), auth("UNIT-OF-MEASURE_READ"));

        assertThat(html).isNotBlank();
        assertThat(html).contains("uom-table-container");
    }

    @Test
    @DisplayName("sec:authorize — UNIT-OF-MEASURE_CREATE shows Add New button")
    void withUomCreate_addButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
            TEMPLATE, modelWithEmptyPage(), auth("UNIT-OF-MEASURE_READ", "UNIT-OF-MEASURE_CREATE"));

        assertThat(html).contains("/inventory/unit-of-measures/create");
    }

    @Test
    @DisplayName("sec:authorize — missing UNIT-OF-MEASURE_CREATE hides Add New button")
    void withoutUomCreate_addButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
            TEMPLATE, modelWithEmptyPage(), auth("UNIT-OF-MEASURE_READ"));

        assertThat(html).doesNotContain("/inventory/unit-of-measures/create");
    }

    @Test
    @DisplayName("sec:authorize — UNIT-OF-MEASURE_UPDATE shows Edit button for each row")
    void withUomUpdate_editButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
            TEMPLATE, modelWithOneUom(), auth("UNIT-OF-MEASURE_READ", "UNIT-OF-MEASURE_UPDATE"));

        assertThat(html).contains("Kilogram");
        assertThat(html).contains("/inventory/unit-of-measures/edit");
    }

    @Test
    @DisplayName("sec:authorize — missing UNIT-OF-MEASURE_UPDATE hides Edit button")
    void withoutUomUpdate_editButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
            TEMPLATE, modelWithOneUom(), auth("UNIT-OF-MEASURE_READ"));

        assertThat(html).contains("Kilogram");
        assertThat(html).doesNotContain("/inventory/unit-of-measures/edit");
    }

    @Test
    @DisplayName("sec:authorize — UNIT-OF-MEASURE_DELETE shows Delete button for each row")
    void withUomDelete_deleteButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
            TEMPLATE, modelWithOneUom(), auth("UNIT-OF-MEASURE_READ", "UNIT-OF-MEASURE_DELETE"));

        assertThat(html).contains("Kilogram");
        assertThat(html).contains("modal-delete-1");
    }

    @Test
    @DisplayName("sec:authorize — missing UNIT-OF-MEASURE_DELETE hides Delete button")
    void withoutUomDelete_deleteButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
            TEMPLATE, modelWithOneUom(), auth("UNIT-OF-MEASURE_READ"));

        assertThat(html).contains("Kilogram");
        assertThat(html).doesNotContain("modal-delete-1");
    }
}
