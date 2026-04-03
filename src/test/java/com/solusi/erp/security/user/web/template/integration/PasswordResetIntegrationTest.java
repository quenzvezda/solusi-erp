package com.solusi.erp.security.user.web.template.integration;

import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration-template")
class PasswordResetIntegrationTest {

    @Test
    void resetPage_rendersForAuthenticatedUser() {
        String html = TemplateTestUtils.renderWithSecurity("security/reset-password", Map.of(),
                new TestingAuthenticationToken("john", "n/a", "USERS_READ"));

        assertThat(html).contains("/reset-password");
    }
}
