package com.solusi.erp.core.web.template;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Static template check for home.html.
 * Verifies key sections and elements exist without Spring context.
 */
@DisplayName("Home — Static Template Check")
@Tag("template")
class HomeTemplateTest {

    private static final String TEMPLATE = "home";

    @Test
    @DisplayName("Template file exists and contains hero section")
    void templateContainsHeroSection() {
        String resourcePath = "templates/" + TEMPLATE + ".html";
        java.io.InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
        assertThat(is).as("Template file must exist: %s", resourcePath).isNotNull();
        String raw;
        try (java.io.InputStream in = is) {
            raw = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        assertThat(raw).contains("hero-section");
    }

    @Test
    @DisplayName("Template contains tech-stack section")
    void templateContainsTechStack() {
        String resourcePath = "templates/" + TEMPLATE + ".html";
        java.io.InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
        assertThat(is).isNotNull();
        String raw;
        try (java.io.InputStream in = is) {
            raw = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        assertThat(raw).contains("tech-stack");
    }

    @Test
    @DisplayName("Template contains login link")
    void templateContainsLoginLink() {
        String resourcePath = "templates/" + TEMPLATE + ".html";
        java.io.InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
        assertThat(is).isNotNull();
        String raw;
        try (java.io.InputStream in = is) {
            raw = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        assertThat(raw).contains("/login");
    }
}
