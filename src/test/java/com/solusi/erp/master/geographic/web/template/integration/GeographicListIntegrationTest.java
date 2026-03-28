package com.solusi.erp.master.geographic.web.template.integration;

import com.solusi.erp.master.geographic.web.dto.GeographicSummaryResponse;
import com.solusi.erp.master.model.GeographicType;
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
 * Integration test for Geographic list template — Thymeleaf rendering with SpringSecurityDialect.
 */
@DisplayName("Geographic List — Template Integration Test (sec:authorize visibility)")
@Tag("integration-template")
class GeographicListIntegrationTest {

    private static final String TEMPLATE = "master/geographic/list";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token =
                new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private Map<String, Object> modelWithEmptyPage() {
        org.springframework.data.domain.Page<GeographicSummaryResponse> emptyPage =
                new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        return Map.of("page", emptyPage, "keyword", "");
    }

    private Map<String, Object> modelWithOneGeographic() {
        GeographicSummaryResponse dto = new GeographicSummaryResponse();
        dto.setId(1L);
        dto.setCode("ID");
        dto.setName("Indonesia");
        dto.setType(GeographicType.COUNTRY);
        dto.setIsActive(Boolean.TRUE);

        org.springframework.data.domain.Page<GeographicSummaryResponse> page =
                new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
        return Map.of("page", page, "keyword", "");
    }

    @Test
    @DisplayName("Template renders without error for user with GEOGRAPHIC_READ")
    void withGeographicRead_templateRendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("GEOGRAPHIC_READ"));

        assertThat(html).isNotBlank();
        assertThat(html).contains("geographic-table-container");
    }

    @Test
    @DisplayName("sec:authorize — GEOGRAPHIC_CREATE shows Add New Geographic button")
    void withGeographicCreate_addButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("GEOGRAPHIC_READ", "GEOGRAPHIC_CREATE"));

        assertThat(html).contains("/master/geographics/create");
    }

    @Test
    @DisplayName("sec:authorize — missing GEOGRAPHIC_CREATE hides Add New Geographic button")
    void withoutGeographicCreate_addButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("GEOGRAPHIC_READ"));

        assertThat(html).doesNotContain("/master/geographics/create");
    }

    @Test
    @DisplayName("sec:authorize — GEOGRAPHIC_UPDATE shows Edit button for each row")
    void withGeographicUpdate_editButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneGeographic(), auth("GEOGRAPHIC_READ", "GEOGRAPHIC_UPDATE"));

        assertThat(html).contains("Indonesia");
        assertThat(html).contains("/master/geographics/edit");
    }

    @Test
    @DisplayName("sec:authorize — missing GEOGRAPHIC_UPDATE hides Edit button")
    void withoutGeographicUpdate_editButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneGeographic(), auth("GEOGRAPHIC_READ"));

        assertThat(html).contains("Indonesia");
        assertThat(html).doesNotContain("/master/geographics/edit");
    }

    @Test
    @DisplayName("sec:authorize — GEOGRAPHIC_DELETE shows Delete button for each row")
    void withGeographicDelete_deleteButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneGeographic(), auth("GEOGRAPHIC_READ", "GEOGRAPHIC_DELETE"));

        assertThat(html).contains("Indonesia");
        assertThat(html).contains("modal-delete-1");
    }

    @Test
    @DisplayName("sec:authorize — missing GEOGRAPHIC_DELETE hides Delete button")
    void withoutGeographicDelete_deleteButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneGeographic(), auth("GEOGRAPHIC_READ"));

        assertThat(html).contains("Indonesia");
        assertThat(html).doesNotContain("modal-delete-1");
    }
}
