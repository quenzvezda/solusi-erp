package com.solusi.erp.accounting.journal.web.template;

import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class JournalMessageBundleTest {

    private static final List<String> MANUAL_JOURNAL_KEYS = List.of(
            "label.schema.event.MANUAL",
            "label.journal.referenceNo",
            "label.journal.currency",
            "label.journal.exchangeRate",
            "label.journal.memo",
            "label.journal.transactionAmount",
            "label.journal.baseAmount",
            "label.journal.balanced",
            "label.journal.unbalanced",
            "label.journal.reversalOf",
            "label.journal.reversedBy",
            "label.create",
            "label.add",
            "label.post",
            "label.reverse",
            "label.exchangeRate",
            "msg.success.journal.created",
            "msg.success.journal.updated",
            "msg.success.journal.deleted",
            "msg.success.journal.posted",
            "msg.success.journal.reversed",
            "msg.confirm.journal.post",
            "msg.confirm.journal.delete",
            "msg.confirm.journal.reverse",
            "msg.error.journal.lines.minimum",
            "msg.error.journal.currency.invalid",
            "msg.error.journal.default.currency.rate.invalid",
            "msg.error.journal.account.invalid",
            "msg.error.journal.manual.required",
            "msg.error.journal.draft.required",
            "msg.error.journal.posted.required",
            "msg.error.journal.already.reversed",
            "msg.error.journal.reversal.chain.not.allowed"
    );

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
        assertManualJournalKeys(props);
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
        assertManualJournalKeys(props);
    }

    private void assertManualJournalKeys(Properties props) {
        for (String key : MANUAL_JOURNAL_KEYS) {
            assertThat(props.getProperty(key)).as("missing message key %s", key).isNotBlank();
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
