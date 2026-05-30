package com.solusi.erp.core.web.template;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class LookupFeedbackTemplateTest {

    @Test
    void messageBundlesContainLookupFeedbackKeys() throws Exception {
        Properties english = loadProperties("messages.properties");
        Properties indonesian = loadProperties("messages_id.properties");

        assertThat(english.getProperty("msg.error.lookup.forbidden"))
                .isEqualTo("You don't have access to this list.");
        assertThat(english.getProperty("msg.error.lookup.failed"))
                .isEqualTo("Failed to load. Please try again.");
        assertThat(english.getProperty("label.lookup.no-results"))
                .isEqualTo("No results found");

        assertThat(indonesian.getProperty("msg.error.lookup.forbidden"))
                .isEqualTo("Anda tidak punya akses ke daftar ini.");
        assertThat(indonesian.getProperty("msg.error.lookup.failed"))
                .isEqualTo("Gagal memuat. Silakan coba lagi.");
        assertThat(indonesian.getProperty("label.lookup.no-results"))
                .isEqualTo("Tidak ada hasil");
    }

    @Test
    void layoutExposesLookupFeedbackMessagesToJavascript() throws Exception {
        String layout = readResource("templates/layout/master.html");

        assertThat(layout)
                .contains("lookupForbidden: /*[[#{msg.error.lookup.forbidden}]]*/")
                .contains("lookupError: /*[[#{msg.error.lookup.failed}]]*/")
                .contains("lookupNoResults: /*[[#{label.lookup.no-results}]]*/");
    }

    private Properties loadProperties(String resource) throws Exception {
        Properties props = new Properties();
        try (InputStream in = getResource(resource);
             InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            props.load(reader);
        }
        return props;
    }

    private String readResource(String resource) throws Exception {
        try (InputStream in = getResource(resource)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private InputStream getResource(String resource) {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(resource);
        assertThat(stream).as("Resource must exist: %s", resource).isNotNull();
        return stream;
    }
}
