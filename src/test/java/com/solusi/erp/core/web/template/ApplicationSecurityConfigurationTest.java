package com.solusi.erp.core.web.template;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationSecurityConfigurationTest {

    @Test
    @DisplayName("Session cookie SameSite is explicitly set to Lax")
    void sessionCookieSameSiteIsLax() {
        Map<String, Object> yaml = readApplicationYaml();

        assertThat(valueAt(yaml, "server", "servlet", "session", "cookie", "same-site"))
                .isEqualTo("lax");
    }

    @Test
    @DisplayName("Production error responses do not expose internal exception details")
    void errorResponsesDoNotExposeInternalDetails() {
        Map<String, Object> yaml = readApplicationYaml();

        assertThat(valueAt(yaml, "spring", "web", "error", "include-message"))
                .isEqualTo("never");
        assertThat(valueAt(yaml, "spring", "web", "error", "include-binding-errors"))
                .isEqualTo("never");
        assertThat(valueAt(yaml, "spring", "web", "error", "include-stacktrace"))
                .isEqualTo("never");
        assertThat(valueAt(yaml, "spring", "web", "error", "whitelabel", "enabled"))
                .isEqualTo(false);
    }

    @SuppressWarnings("unchecked")
    private static Object valueAt(Map<String, Object> yaml, String... path) {
        Object current = yaml;
        for (String key : path) {
            assertThat(current)
                    .as("Path segment %s should resolve within %s", key, String.join(".", path))
                    .isInstanceOf(Map.class);
            current = ((Map<String, Object>) current).get(key);
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> readApplicationYaml() {
        try (InputStream inputStream = ApplicationSecurityConfigurationTest.class
                .getClassLoader()
                .getResourceAsStream("application.yaml")) {
            assertThat(inputStream).as("application.yaml must exist").isNotNull();
            return new Yaml().loadAs(inputStream, Map.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read application.yaml", e);
        }
    }
}
