package com.solusi.erp.security.user.web.template;

import com.solusi.erp.security.user.web.dto.ProfileSaveRequest;
import com.solusi.erp.security.user.web.dto.ProfileResponse;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileTemplateTest {

    @Test
    void profileViewTemplate_rendersName() {
        ProfileResponse profile = new ProfileResponse();
        profile.setFullName("John Doe");
        profile.setUsername("john");
        profile.setEmail("john@test.com");
        profile.setTheme("light");
        profile.setLanguageCode("id");
        profile.setDefaultPageSize(10);

        String html = TemplateTestUtils.renderWithSecurity("security/profile/view", Map.of("profile", profile),
                new org.springframework.security.authentication.TestingAuthenticationToken("u", "n", "USERS_READ"));

        assertThat(html).contains("John Doe");
    }
}
