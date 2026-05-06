package com.solusi.erp.accounting.journal.web.template;

import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class JournalMessageBundleTest {

    @Test
    void englishBundle_containsJournalKeys() throws Exception {
        Properties props = load("messages.properties");

        assertThat(props.getProperty("label.all")).isEqualTo("All");
        assertThat(props.getProperty("label.detail")).isEqualTo("Detail");
        assertThat(props.getProperty("label.description")).isEqualTo("Description");
        assertThat(props.getProperty("label.journal.account")).isEqualTo("Account");
        for (SchemaEventType type : SchemaEventType.values()) {
            assertThat(props.getProperty("label.schema.event." + type.name())).isNotBlank();
        }
    }

    @Test
    void indonesianBundle_containsJournalKeys() throws Exception {
        Properties props = load("messages_id.properties");

        assertThat(props.getProperty("label.all")).isEqualTo("Semua");
        assertThat(props.getProperty("label.detail")).isEqualTo("Detail");
        assertThat(props.getProperty("label.description")).isEqualTo("Deskripsi");
        assertThat(props.getProperty("label.journal.account")).isEqualTo("Akun");
        for (SchemaEventType type : SchemaEventType.values()) {
            assertThat(props.getProperty("label.schema.event." + type.name())).isNotBlank();
        }
    }

    private Properties load(String resource) throws Exception {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(resource);
        assertThat(stream).as("Message bundle not found: %s", resource).isNotNull();

        Properties props = new Properties();
        try (InputStream in = stream; InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            props.load(reader);
        }
        return props;
    }
}
