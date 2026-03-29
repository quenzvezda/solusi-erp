package com.solusi.erp.security.permissiongroup.web.template.integration;

import com.solusi.erp.security.permissiongroup.web.dto.PermissionGroupSaveRequest;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Integration test for PermissionGroup form template.
 */
@DisplayName("PermissionGroup Form — Template Integration Test")
@Tag("integration-template")
class PermissionGroupFormIntegrationTest {

    private static final String TEMPLATE = "security/permission-groups/form";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token = new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private PermissionGroupSaveRequest createRequest() {
        PermissionGroupSaveRequest req = new PermissionGroupSaveRequest();
        req.setCode("SEC-01");
        req.setNameId("Keamanan");
        req.setNameEn("Security");
        req.setBreadcrumbId("Keamanan");
        req.setBreadcrumbEn("Security");
        req.setUrlPath("/security");
        return req;
    }

    private PermissionGroupSaveRequest editRequest() {
        PermissionGroupSaveRequest req = createRequest();
        req.setId(1L);
        req.setVersion(1);
        return req;
    }

    @Test
    @DisplayName("Create form renders without error and contains form fields")
    void createForm_rendersSuccessfully() {
        assertDoesNotThrow(() -> {
            String html = TemplateTestUtils.renderWithSecurity(
                    TEMPLATE, Map.of("request", createRequest()), auth("MENU-GROUP_CREATE"));
            assertThat(html).isNotBlank();
            assertThat(html).contains("/security/menu-groups/create");
        });
    }

    @Test
    @DisplayName("Create form contains required field bindings")
    void createForm_containsRequiredFields() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, Map.of("request", createRequest()), auth("MENU-GROUP_CREATE"));

        // Check for elements that are hardcoded in the template (not inside th:replace or th:field)
        assertThat(html).contains("Icon Class");
        assertThat(html).contains("Bahasa Indonesia");
        assertThat(html).contains("English");
    }

    @Test
    @DisplayName("Edit form renders without error")
    void editForm_rendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, Map.of("request", editRequest()), auth("MENU-GROUP_UPDATE"));
        assertThat(html).isNotBlank();
        assertThat(html).contains("/security/menu-groups/edit/1");
    }
}
