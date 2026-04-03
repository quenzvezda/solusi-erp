package com.solusi.erp.security.permission.web.template;

import com.solusi.erp.security.permission.web.dto.PermissionSaveRequest;
import com.solusi.erp.security.permission.web.dto.PermissionSummaryResponse;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.Test;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionTemplateTest {

    @Test
    void listTemplate_rendersContainer() {
        PermissionSummaryResponse dto = new PermissionSummaryResponse();
        dto.setId(1L);
        dto.setName("INV_READ");
        Map<String, List<PermissionSummaryResponse>> grouped = Map.of("INV", List.of(dto));

        Context ctx = new Context();
        ctx.setVariables(Map.of(
                "groupedPermissions", grouped,
                "permissionGroups", List.of(),
                "permissionRequest", new PermissionSaveRequest()
        ));

        String html = TemplateTestUtils.renderFragment(
                "security/permissions/list", "perm-list-content", ctx);

        assertThat(html).contains("perm-list-content");
    }
}
