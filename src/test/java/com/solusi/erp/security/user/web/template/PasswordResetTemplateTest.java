package com.solusi.erp.security.user.web.template;

import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordResetTemplateTest {

    @Test
    void resetTemplate_containsFormAction() {
        String html = TemplateTestUtils.renderFragment("security/reset-password", "form-footer", new org.thymeleaf.context.Context());

        assertThat(html).contains("/reset-password");
    }
}
