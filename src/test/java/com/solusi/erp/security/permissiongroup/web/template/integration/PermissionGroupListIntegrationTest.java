package com.solusi.erp.security.permissiongroup.web.template.integration;

import com.solusi.erp.security.permissiongroup.web.dto.PermissionGroupSummaryResponse;
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
 * Integration test for PermissionGroup list template — Thymeleaf rendering with SpringSecurityDialect.
 */
@DisplayName("PermissionGroup List — Template Integration Test (sec:authorize visibility)")
@Tag("integration-template")
class PermissionGroupListIntegrationTest {

    private static final String TEMPLATE = "security/permission-groups/list";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token =
                new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private Map<String, Object> modelWithEmptyPage() {
        org.springframework.data.domain.Page<PermissionGroupSummaryResponse> emptyPage =
                new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        return Map.of("page", emptyPage, "keyword", "");
    }

    private Map<String, Object> modelWithOneGroup() {
        PermissionGroupSummaryResponse dto = new PermissionGroupSummaryResponse();
        dto.setId(1L);
        dto.setCode("SEC-01");
        dto.setNameId("Keamanan");
        dto.setNameEn("Security");
        dto.setBreadcrumbId("Keamanan");
        dto.setBreadcrumbEn("Security");
        dto.setUrlPath("/security");
        dto.setIconClass("ti-shield");

        org.springframework.data.domain.Page<PermissionGroupSummaryResponse> page =
                new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
        return Map.of("page", page, "keyword", "");
    }

    @Test
    @DisplayName("Template renders without error for user with MENU-GROUP_READ")
    void withMenuGroupRead_templateRendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("MENU-GROUP_READ"));

        assertThat(html).isNotBlank();
        assertThat(html).contains("menu-group-table-container");
    }

    @Test
    @DisplayName("sec:authorize — MENU-GROUP_CREATE shows Add New button")
    void withMenuGroupCreate_addButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("MENU-GROUP_READ", "MENU-GROUP_CREATE"));

        assertThat(html).contains("/security/menu-groups/create");
    }

    @Test
    @DisplayName("sec:authorize — missing MENU-GROUP_CREATE hides Add New button")
    void withoutMenuGroupCreate_addButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("MENU-GROUP_READ"));

        assertThat(html).doesNotContain("/security/menu-groups/create");
    }

    @Test
    @DisplayName("sec:authorize — MENU-GROUP_UPDATE shows Edit button for each row")
    void withMenuGroupUpdate_editButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneGroup(), auth("MENU-GROUP_READ", "MENU-GROUP_UPDATE"));

        assertThat(html).contains("Keamanan");
        assertThat(html).contains("/security/menu-groups/edit");
    }

    @Test
    @DisplayName("sec:authorize — missing MENU-GROUP_UPDATE hides Edit button")
    void withoutMenuGroupUpdate_editButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneGroup(), auth("MENU-GROUP_READ"));

        assertThat(html).contains("Keamanan");
        assertThat(html).doesNotContain("/security/menu-groups/edit");
    }

    @Test
    @DisplayName("sec:authorize — MENU-GROUP_DELETE shows Delete button for each row")
    void withMenuGroupDelete_deleteButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneGroup(), auth("MENU-GROUP_READ", "MENU-GROUP_DELETE"));

        assertThat(html).contains("Keamanan");
        assertThat(html).contains("modal-delete-1");
    }

    @Test
    @DisplayName("sec:authorize — missing MENU-GROUP_DELETE hides Delete button")
    void withoutMenuGroupDelete_deleteButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneGroup(), auth("MENU-GROUP_READ"));

        assertThat(html).contains("Keamanan");
        assertThat(html).doesNotContain("modal-delete-1");
    }
}
