package com.solusi.erp.core.web.template.integration;

import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for home.html — Thymeleaf rendering with SpringSecurityDialect.
 *
 * <p>The home page is the public landing page. It should render without authentication,
 * but we test with an auth context to ensure the security dialect doesn't break it.</p>
 */
@DisplayName("Home — Template Integration Test")
@Tag("integration-template")
class HomeIntegrationTest {

    private static final String TEMPLATE = "home";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token =
                new TestingAuthenticationToken("visitor", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    @Test
    @DisplayName("Template renders without error")
    void templateRendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, Map.of(), auth());

        assertThat(html).isNotBlank();
        assertThat(html).contains("hero-section");
    }

    @Test
    @DisplayName("Template contains tech-stack section")
    void templateContainsTechStack() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, Map.of(), auth());

        assertThat(html).contains("tech-stack");
    }

    @Test
    @DisplayName("Template contains login link")
    void templateContainsLoginLink() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, Map.of(), auth());

        assertThat(html).contains("/login");
    }
}
