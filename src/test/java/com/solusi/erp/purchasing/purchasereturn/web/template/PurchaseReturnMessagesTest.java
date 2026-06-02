package com.solusi.erp.purchasing.purchasereturn.web.template;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class PurchaseReturnMessagesTest {

    private static final List<String> KEYS = List.of(
            "label.purchase-return.title", "label.purchase-return.subtitle",
            "label.purchase-return.create", "label.purchase-return.edit", "label.purchase-return.detail",
            "label.purchase-return.code", "label.purchase-return.return-date",
            "label.purchase-return.receipt-date", "label.purchase-return.receipt-date-from",
            "label.purchase-return.receipt-date-to", "label.purchase-return.supplier",
            "label.purchase-return.facility", "label.purchase-return.currency",
            "label.purchase-return.exchange-rate", "label.purchase-return.goods-receipt",
            "label.purchase-return.purchase-order", "label.purchase-return.goods-issue",
            "label.purchase-return.reason", "label.purchase-return.note", "label.purchase-return.status",
            "label.purchase-return.lines", "label.purchase-return.line.add", "label.purchase-return.line.empty",
            "label.purchase-return.empty", "label.purchase-return.select-source",
            "label.purchase-return.select-source.subtitle", "label.purchase-return.select-source.empty",
            "label.purchase-return.selector.search", "label.purchase-return.selector.gr-line.empty",
            "label.purchase-return.serial.select", "label.purchase-return.serial.search",
            "label.purchase-return.selector.serial.empty", "label.purchase-return.eligible-lines",
            "label.purchase-return.returnable-qty", "label.purchase-return.summary.lines",
            "label.purchase-return.reservation-summary", "label.purchase-return.approver",
            "label.purchase-return.submit", "label.purchase-return.confirm", "label.purchase-return.cancel",
            "label.purchase-return.cancel-submission", "label.purchase-return.continue",
            "msg.success.purchase-return.created", "msg.success.purchase-return.updated",
            "msg.success.purchase-return.submitted", "msg.success.purchase-return.confirmed",
            "msg.success.purchase-return.cancelled", "msg.confirm.purchase-return.submit",
            "msg.confirm.purchase-return.confirm", "msg.confirm.purchase-return.cancel",
            "msg.confirm.purchase-return.cancel-submission", "msg.purchase-return.cancel-submission.notes",
            "msg.warning.purchase-return.phase-one-accounting", "msg.error.purchase-return.not-found",
            "msg.error.purchase-return.auth-required", "msg.error.purchase-return.requester-party-required",
            "msg.error.purchase-return.source.goods-receipt-required", "msg.error.purchase-return.source.ineligible",
            "msg.error.purchase-return.lines-required", "msg.error.purchase-return.reason-required",
            "msg.error.purchase-return.other-note-required", "msg.error.purchase-return.line.required",
            "msg.error.purchase-return.line.reason-required", "msg.error.purchase-return.line.other-note-required",
            "msg.error.purchase-return.line.quantity-positive", "msg.error.purchase-return.line.quantity-exceeded",
            "msg.error.purchase-return.line.quantity-exceeds-returnable",
            "msg.error.purchase-return.line.valuation-reference-required",
            "msg.error.purchase-return.line.serial-required", "msg.error.purchase-return.line.serial-count-mismatch",
            "msg.error.purchase-return.line.serial-whole-quantity",
            "msg.error.purchase-return.line.serial-selection-invalid",
            "msg.error.purchase-return.line.serial-stale", "msg.error.purchase-return.line.stale",
            "msg.error.purchase-return.selector.select-at-least-one",
            "msg.error.purchase-return.submit.approver-required", "msg.error.purchase-return.submit.user-required",
            "msg.error.purchase-return.submit.invalid-status", "msg.error.purchase-return.update.not-draft",
            "msg.error.purchase-return.cancel-draft.invalid-status",
            "msg.error.purchase-return.cancel-submission.invalid-status",
            "msg.error.purchase-return.cancel-submission.creator-only",
            "msg.error.purchase-return.cancel-approved.invalid-status",
            "msg.error.purchase-return.approve.invalid-status", "msg.error.purchase-return.reject.invalid-status",
            "msg.error.purchase-return.confirm.invalid-status",
            "msg.error.purchase-return.confirm.goods-issue-exists",
            "msg.error.purchase-return.confirm.gi-required", "msg.error.purchase-return.reservation.failed",
            "msg.error.purchase-return.reservation.serial-failed"
    );

    @Test
    void englishBundle_containsPurchaseReturnKeys() throws Exception {
        assertKeys(load("messages.properties"));
    }

    @Test
    void indonesianBundle_containsPurchaseReturnKeys() throws Exception {
        assertKeys(load("messages_id.properties"));
    }

    private void assertKeys(Properties properties) {
        for (String key : KEYS) {
            assertThat(properties.getProperty(key)).as("missing message key %s", key).isNotBlank();
        }
        for (String status : List.of("draft", "submitted", "approved", "rejected", "cancelled", "confirmed")) {
            assertThat(properties.getProperty("label.purchase-return.status." + status))
                    .as("missing status message %s", status).isNotBlank();
        }
        for (String reason : List.of("damaged", "wrong-item", "quality-issue", "over-receipt", "expired", "other")) {
            assertThat(properties.getProperty("label.purchase-return.reason." + reason))
                    .as("missing reason message %s", reason).isNotBlank();
        }
    }

    private Properties load(String resource) throws Exception {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(resource);
        assertThat(stream).as("Message bundle not found: %s", resource).isNotNull();
        Properties properties = new Properties();
        try (InputStream in = stream; InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties;
    }
}
