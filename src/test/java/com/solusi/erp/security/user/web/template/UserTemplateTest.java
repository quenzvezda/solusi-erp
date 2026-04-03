package com.solusi.erp.security.user.web.template;

import com.solusi.erp.security.user.web.dto.UserSaveRequest;
import com.solusi.erp.security.user.web.dto.UserSummaryResponse;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UserTemplateTest {

    @Test
    void listTemplate_rendersUserContent() {
        UserSummaryResponse row = new com.solusi.erp.security.user.web.dto.UserDetailResponse();
        row.setId(1L);
        row.setUsername("john");
        row.setEmail("john@test.com");
        row.setFullName("John Doe");
        row.setRoleName("ROLE_USER");
        row.setEnabled(true);
        row.setPasswordChangeRequired(false);

        Map<String, Object> model = Map.of(
                "page", new org.springframework.data.domain.PageImpl<>(List.of(row), org.springframework.data.domain.PageRequest.of(0, 10), 1),
                "keyword", ""
        );

        String html = TemplateTestUtils.renderWithSecurity("security/users/list", model,
                new org.springframework.security.authentication.TestingAuthenticationToken("u", "n", "USERS_READ"));

        assertThat(html).contains("John Doe");
    }
}
