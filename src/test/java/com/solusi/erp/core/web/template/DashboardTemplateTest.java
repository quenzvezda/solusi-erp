package com.solusi.erp.core.web.template;

import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.thymeleaf.context.Context;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Static template check for dashboard/index.html.
 * Verifies fragment IDs and placeholder expressions exist without Spring context.
 */
@DisplayName("Dashboard — Static Template Check")
@Tag("template")
class DashboardTemplateTest {

    private static final String TEMPLATE = "dashboard/index";

    @Test
    @DisplayName("Template contains dashboard-content div")
    void templateContainsDashboardContent() {
        String html = TemplateTestUtils.renderFragment(TEMPLATE, "dashboard-content", new Context(Locale.getDefault()));
        assertThat(html).contains("dashboard-content");
    }

    @Test
    @DisplayName("Template contains page-title element")
    void templateContainsPageTitle() {
        String resourcePath = "templates/" + TEMPLATE + ".html";
        java.io.InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
        assertThat(is).as("Template file must exist: %s", resourcePath).isNotNull();
        String raw;
        try (java.io.InputStream in = is) {
            raw = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        assertThat(raw).contains("page-title");
        assertThat(raw).contains("#authentication.name");
    }
}
