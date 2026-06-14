package com.solusi.erp.inventory.goodsissue.web.template;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class GoodsIssueMessageBundleTest {

    private static final List<String> REVERSAL_KEYS = List.of(
            "label.summary",
            "label.gi.header",
            "label.gi.reversalDate",
            "label.gi.cancel.reason",
            "label.gi.cancel.lines",
            "label.gi.cancel.historicalContainer",
            "label.gi.cancel.targetContainer",
            "label.gi.cancel.lineCount",
            "label.gi.cancel.targetCount",
            "label.gi.journal.original",
            "label.gi.journal.reversal",
            "msg.error.gi.cancel.reversal.date.required",
            "msg.error.gi.cancel.source.owned",
            "msg.error.gi.cancel.movements.notfound",
            "msg.error.gi.cancel.journal.notfound",
            "validation.gi.cancel.reversalDate.required",
            "validation.gi.cancel.reason.required",
            "validation.gi.cancel.lines.required",
            "validation.gi.cancel.originalMovement.required",
            "validation.gi.cancel.targetContainer.required",
            "msg.error.stock.reversal.requests.required",
            "msg.error.stock.reversal.original.required",
            "msg.error.stock.reversal.target.container.required",
            "msg.error.stock.reversal.date.required",
            "msg.error.stock.reversal.original.not.found",
            "msg.error.stock.reversal.already.reversed",
            "msg.error.stock.reversal.target.container.not.found",
            "msg.error.stock.reversal.target.container.inactive",
            "msg.error.stock.reversal.chain.not.allowed",
            "msg.error.stock.reversal.outbound.required",
            "msg.error.stock.reversal.quantity.required",
            "msg.error.stock.reversal.original.container.not.found",
            "msg.error.stock.reversal.original.grid.not.found",
            "msg.error.stock.reversal.target.grid.not.found",
            "msg.error.stock.reversal.facility.mismatch",
            "msg.error.stock.reversal.serial.on.hand"
    );

    @Test
    void englishBundle_containsGoodsIssueReversalKeys() throws Exception {
        assertReversalKeys(load("messages.properties"));
    }

    @Test
    void indonesianBundle_containsGoodsIssueReversalKeys() throws Exception {
        assertReversalKeys(load("messages_id.properties"));
    }

    private void assertReversalKeys(Properties props) {
        for (String key : REVERSAL_KEYS) {
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
