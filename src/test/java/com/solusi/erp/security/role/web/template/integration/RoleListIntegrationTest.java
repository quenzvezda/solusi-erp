package com.solusi.erp.security.role.web.template.integration;

import com.solusi.erp.security.permission.web.dto.PermissionSummaryResponse;
import com.solusi.erp.security.role.web.dto.RoleSummaryResponse;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Role List — Template Integration Test (sec:authorize visibility)")
@Tag("integration-template")
class RoleListIntegrationTest {

    private static final String TEMPLATE = "security/roles/list";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token =
                new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private Map<String, Object> modelWithEmptyList() {
        return Map.of("roles", List.of());
    }

    private Map<String, Object> modelWithOneRole() {
        PermissionSummaryResponse perm = new PermissionSummaryResponse();
        perm.setId(1L);
        perm.setName("USERS_READ");
        RoleSummaryResponse role = new RoleSummaryResponse();
        role.setId(1L);
        role.setName("ROLE_USER");
        role.setDescription("User role");
        role.setPermissions(Set.of(perm));
        return Map.of("roles", List.of(role));
    }

    @Test
    @DisplayName("Template renders without error for user with ROLES_READ")
    void withRolesRead_templateRendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyList(), auth("ROLES_READ"));

        assertThat(html).isNotBlank();
        assertThat(html).contains("role-table-container");
    }

    @Test
    @DisplayName("sec:authorize — ROLES_CREATE shows Add New button")
    void withRolesCreate_addButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyList(), auth("ROLES_READ", "ROLES_CREATE"));

        assertThat(html).contains("/security/roles/create");
    }

    @Test
    @DisplayName("sec:authorize — missing ROLES_CREATE hides Add New button")
    void withoutRolesCreate_addButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyList(), auth("ROLES_READ"));

        assertThat(html).doesNotContain("/security/roles/create");
    }

    @Test
    @DisplayName("sec:authorize — ROLES_UPDATE shows Edit button")
    void withRolesUpdate_editButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneRole(), auth("ROLES_READ", "ROLES_UPDATE"));

        assertThat(html).contains("ROLE_USER");
        assertThat(html).contains("/security/roles/edit");
    }

    @Test
    @DisplayName("sec:authorize — ROLES_DELETE shows Delete button")
    void withRolesDelete_deleteButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneRole(), auth("ROLES_READ", "ROLES_DELETE"));

        assertThat(html).contains("ROLE_USER");
        assertThat(html).contains("modal-delete-1");
    }

    @Test
    @DisplayName("sec:authorize — missing ROLES_UPDATE hides Edit button")
    void withoutRolesUpdate_editButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneRole(), auth("ROLES_READ"));

        assertThat(html).contains("ROLE_USER");
        assertThat(html).doesNotContain("/security/roles/edit/1");
    }

    @Test
    @DisplayName("sec:authorize — missing ROLES_DELETE hides Delete button")
    void withoutRolesDelete_deleteButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneRole(), auth("ROLES_READ"));

        assertThat(html).contains("ROLE_USER");
        assertThat(html).doesNotContain("modal-delete-1");
    }
}

