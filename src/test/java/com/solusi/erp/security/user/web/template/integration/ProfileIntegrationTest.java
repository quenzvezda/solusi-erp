package com.solusi.erp.security.user.web.template.integration;

import com.solusi.erp.security.user.web.dto.ProfileResponse;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration-template")
class ProfileIntegrationTest {

    @Test
    void profilePage_rendersForAuthenticatedUser() {
        ProfileResponse profile = new ProfileResponse();
        profile.setFullName("John Doe");
        profile.setUsername("john");
        profile.setEmail("john@test.com");
        profile.setLanguageCode("id");
        profile.setTheme("light");
        profile.setDefaultPageSize(10);

        String html = TemplateTestUtils.renderWithSecurity("security/profile/view", Map.of("profile", profile),
                new TestingAuthenticationToken("john", "n/a", "USERS_READ"));

        assertThat(html).contains("/profile/edit");
    }
}
