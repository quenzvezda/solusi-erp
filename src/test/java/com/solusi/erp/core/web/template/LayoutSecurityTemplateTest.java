package com.solusi.erp.core.web.template;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class LayoutSecurityTemplateTest {

    @Test
    @DisplayName("Layout body only renders HTMX CSRF headers when CSRF is available")
    void layoutBodyGuardsCsrfHtmxHeaders() {
        String template = readTemplate("layout/master.html");

        assertThat(linesContaining(template, "_csrf.token"))
                .anySatisfy(line -> assertThat(line).contains("_csrf != null"));

        String bodyTag = openingBodyTag(template) + "</body>";
        String html = render(bodyTag);

        assertThat(html).contains("data-bs-theme=\"light\"");
        assertThat(html).doesNotContain("hx-headers");
    }

    @Test
    @DisplayName("Head fragment only renders CSRF meta tags when CSRF is available")
    void headFragmentGuardsCsrfMetaTags() {
        String template = readTemplate("fragments/head.html");

        assertThat(linesContaining(template, "_csrf.token"))
                .allSatisfy(line -> assertThat(line).contains("_csrf != null"));
        assertThat(linesContaining(template, "_csrf.headerName"))
                .allSatisfy(line -> assertThat(line).contains("_csrf != null"));
        assertThat(linesContaining(template, "_csrf.parameterName"))
                .allSatisfy(line -> assertThat(line).contains("_csrf != null"));

        String csrfMetaTags = String.join("\n", linesContaining(template, "_csrf."));
        String html = render("<head>" + csrfMetaTags + "</head>");

        assertThat(html).doesNotContain("_csrf");
    }

    private static String render(String template) {
        StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCacheable(false);

        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);

        return engine.process(template, new Context(Locale.ENGLISH));
    }

    private static String[] linesContaining(String template, String text) {
        return Arrays.stream(template.split("\\R"))
                .filter(line -> line.contains(text))
                .toArray(String[]::new);
    }

    private static String openingBodyTag(String template) {
        String[] lines = template.split("\\R");
        for (int i = 0; i < lines.length - 1; i++) {
            if (lines[i].contains("<body th:fragment")) {
                return lines[i] + "\n" + lines[i + 1];
            }
        }
        throw new AssertionError("layout/master.html must contain a body fragment tag");
    }

    private static String readTemplate(String path) {
        String resourcePath = "templates/" + path;
        try (InputStream inputStream = LayoutSecurityTemplateTest.class
                .getClassLoader()
                .getResourceAsStream(resourcePath)) {
            assertThat(inputStream)
                    .as("Template resource %s must exist", resourcePath)
                    .isNotNull();
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read template " + resourcePath, e);
        }
    }
}
