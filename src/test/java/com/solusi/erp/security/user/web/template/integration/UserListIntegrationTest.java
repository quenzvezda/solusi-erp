package com.solusi.erp.security.user.web.template.integration;

import com.solusi.erp.security.user.web.dto.UserSummaryResponse;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration-template")
class UserListIntegrationTest {

    @Test
    void withReadAuthority_rendersList() {
        UserSummaryResponse row = new com.solusi.erp.security.user.web.dto.UserDetailResponse();
        row.setId(1L);
        row.setUsername("john");
        row.setEmail("john@test.com");
        row.setFullName("John Doe");
        row.setRoleName("ROLE_USER");
        row.setEnabled(true);

        String html = TemplateTestUtils.renderWithSecurity("security/users/list", Map.of(
                        "page", new org.springframework.data.domain.PageImpl<>(List.of(row), org.springframework.data.domain.PageRequest.of(0, 10), 1),
                        "keyword", ""
                ),
                new TestingAuthenticationToken("john", "n/a", "USERS_READ"));

        assertThat(html).contains("John Doe");
    }
}
