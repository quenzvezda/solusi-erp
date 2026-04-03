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
 * Integration test for dashboard/index.html — Thymeleaf rendering with SpringSecurityDialect.
 *
 * <p>The dashboard template uses {@code #authentication.name} to display the logged-in
 * username. This test verifies the template renders correctly with a security context.</p>
 */
@DisplayName("Dashboard — Template Integration Test")
@Tag("integration-template")
class DashboardIntegrationTest {

    private static final String TEMPLATE = "dashboard/index";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token =
                new TestingAuthenticationToken("adminuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    @Test
    @DisplayName("Template renders without error for user with DASHBOARD_READ")
    void withDashboardRead_templateRendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, Map.of(), auth("DASHBOARD_READ"));

        assertThat(html).isNotBlank();
        assertThat(html).contains("dashboard-content");
    }

    @Test
    @DisplayName("Template contains page-header structure")
    void templateContainsPageHeader() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, Map.of(), auth("DASHBOARD_READ"));

        assertThat(html).contains("page-header");
        assertThat(html).contains("page-title");
    }
}
