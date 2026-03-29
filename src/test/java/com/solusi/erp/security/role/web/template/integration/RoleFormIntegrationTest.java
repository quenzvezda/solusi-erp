package com.solusi.erp.security.role.web.template.integration;

import com.solusi.erp.security.permission.web.dto.PermissionSummaryResponse;
import com.solusi.erp.security.role.web.dto.RoleSaveRequest;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("Role Form — Template Integration Test")
@Tag("integration-template")
class RoleFormIntegrationTest {

    private static final String TEMPLATE = "security/roles/form";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token = new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private RoleSaveRequest createRequest() {
        RoleSaveRequest req = new RoleSaveRequest();
        req.setName("ROLE_USER");
        req.setDescription("User role");
        req.setPermissionIds(Set.of(1L));
        return req;
    }

    private RoleSaveRequest editRequest() {
        RoleSaveRequest req = createRequest();
        req.setId(1L);
        req.setVersion(1);
        return req;
    }

    private Map<String, List<PermissionSummaryResponse>> groupedPermissions() {
        PermissionSummaryResponse permission = new PermissionSummaryResponse();
        permission.setId(1L);
        permission.setName("USERS_READ");
        permission.setDescription("Read users");
        return Map.of("USERS", List.of(permission));
    }

    private Map<String, Object> createModel(RoleSaveRequest request) {
        return Map.of(
                "roleRequest", request,
                "groupedPermissions", groupedPermissions(),
                "auditInfo", new com.solusi.erp.security.role.web.dto.RoleDetailResponse()
        );
    }

    @Test
    @DisplayName("Create form renders without error")
    void createForm_rendersSuccessfully() {
        assertDoesNotThrow(() -> {
            Context ctx = new Context();
            ctx.setVariables(createModel(createRequest()));
            String html = TemplateTestUtils.renderFragment(TEMPLATE, "role-form-content", ctx);
            assertThat(html).isNotBlank();
            assertThat(html).contains("/security/roles/create");
        });
    }

    @Test
    @DisplayName("Edit form renders without error")
    void editForm_rendersSuccessfully() {
        Context ctx = new Context();
        ctx.setVariables(createModel(editRequest()));
        String html = TemplateTestUtils.renderFragment(TEMPLATE, "role-form-content", ctx);
        assertThat(html).isNotBlank();
        assertThat(html).contains("/security/roles/edit/");
    }
}

