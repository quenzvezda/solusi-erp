package com.solusi.erp.security.permission.web.template.integration;

import com.solusi.erp.security.permission.web.dto.PermissionSaveRequest;
import com.solusi.erp.security.permission.web.dto.PermissionSummaryResponse;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Permission Form — Template Integration Test")
@Tag("integration-template")
class PermissionFormIntegrationTest {

    private static final String TEMPLATE = "security/permissions/list";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token = new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private Map<String, Object> model() {
        PermissionSaveRequest request = new PermissionSaveRequest();
        request.setName("INV_READ");
        request.setDescription("Read inventory");

        PermissionSummaryResponse dto = new PermissionSummaryResponse();
        dto.setId(1L);
        dto.setName("INV_READ");

        Map<String, List<PermissionSummaryResponse>> grouped = Map.of("INV", List.of(dto));

        return Map.of(
                "groupedPermissions", grouped,
                "permissionGroups", List.of(),
                "permissionRequest", request
        );
    }

    @Test
    void createFormSection_rendersForCreator() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, model(), auth("PERMISSIONS_READ", "PERMISSIONS_CREATE"));

        assertThat(html).contains("/security/permissions/create");
        assertThat(html).contains("/security/permissions/batch");
    }
}
