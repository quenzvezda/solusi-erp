package com.solusi.erp.security.user.web.template.integration;

import com.solusi.erp.security.user.web.dto.UserSaveRequest;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration-template")
class UserFormIntegrationTest {

    @Test
    void withUpdateAuthority_rendersForm() {
        UserSaveRequest request = new UserSaveRequest();
        request.setId(1L);
        request.setUsername("john");
        request.setEmail("john@test.com");

        Map<String, Object> model = new HashMap<>();
        model.put("userRequest", request);
        model.put("roles", List.of());
        model.put("userUIForm", null);
        model.put("auditInfo", null);

        String html = TemplateTestUtils.renderFragment("security/users/form", "user-form-content", new org.thymeleaf.context.Context(java.util.Locale.getDefault(), model));

        assertThat(html).contains("/security/users/edit/");
    }
}
