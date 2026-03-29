package com.solusi.erp.security.permissiongroup.web.template;

import com.solusi.erp.security.permissiongroup.web.dto.PermissionGroupSaveRequest;
import com.solusi.erp.security.permissiongroup.web.dto.PermissionGroupSummaryResponse;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionGroupTemplateTest {

    @Test
    void listTemplate_rendersTableContainer() {
        PageImpl<PermissionGroupSummaryResponse> emptyPage =
                new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        Context ctx = new Context();
        ctx.setVariables(Map.of("page", emptyPage, "keyword", ""));

        String html = TemplateTestUtils.renderFragment(
                "security/permission-groups/list", "menu-group-table-container", ctx);

        assertThat(html).contains("menu-group-table-container");
    }

    @Test
    void formTemplate_rendersCodeField() {
        PermissionGroupSaveRequest request = new PermissionGroupSaveRequest();
        Context ctx = new Context();
        ctx.setVariables(Map.of("request", request));

        String html = TemplateTestUtils.renderFragment(
                "security/permission-groups/form", "pg-form-content", ctx);

        // Check for hardcoded text that is directly in the template (not inside th:replace)
        assertThat(html).contains("Icon Class");
        assertThat(html).contains("Bahasa Indonesia");
    }
}
