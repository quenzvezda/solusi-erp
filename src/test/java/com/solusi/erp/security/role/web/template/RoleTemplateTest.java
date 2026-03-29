package com.solusi.erp.security.role.web.template;

import com.solusi.erp.security.role.web.dto.RoleSaveRequest;
import com.solusi.erp.security.role.web.dto.RoleSummaryResponse;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.Test;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RoleTemplateTest {

    @Test
    void listTemplate_rendersContainer() {
        RoleSummaryResponse dto = new RoleSummaryResponse();
        dto.setId(1L);
        dto.setName("ROLE_USER");
        dto.setPermissions(Set.of());

        Context ctx = new Context();
        ctx.setVariables(Map.of("roles", List.of(dto)));

        String html = TemplateTestUtils.renderFragment(
                "security/roles/list", "role-table-container", ctx);

        assertThat(html).contains("role-table-container");
    }

    @Test
    void formTemplate_rendersPermissionHint() {
        Context ctx = new Context();
        ctx.setVariables(Map.of(
                "roleRequest", new RoleSaveRequest(),
                "groupedPermissions", Map.of("USERS", List.of())
        ));

        String html = TemplateTestUtils.renderFragment(
                "security/roles/form", "role-form-content", ctx);

        assertThat(html).contains("feature-card");
    }
}

