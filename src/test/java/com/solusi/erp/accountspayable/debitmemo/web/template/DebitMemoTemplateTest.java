package com.solusi.erp.accountspayable.debitmemo.web.template;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.assertj.core.api.Assertions.assertThat;

class DebitMemoTemplateTest {

    @Test
    void list_template_should_expose_filters_and_view_action() throws Exception {
        String template = readResource("templates/accountspayable/debit-memos/list.html");

        assertThat(template).contains("hasAuthority('DEBIT-MEMO_READ')");
        assertThat(template).contains("name=\"keyword\"");
        assertThat(template).contains("name=\"vendorId\"");
        assertThat(template).contains("name=\"settlementStatus\"");
        assertThat(template).contains("name=\"memoDateFrom\"");
        assertThat(template).contains("name=\"memoDateTo\"");
        assertThat(template).contains("data-picker=\"date\"");
        assertThat(template).contains("label.debit-memo.gross");
        assertThat(template).contains("label.debit-memo.remaining");
        assertThat(template).contains("/accounts-payable/debit-memos/{id}");
        assertThat(template).contains("fragments/table :: pagination(${page})");
        assertThat(template).contains("colspan=\"10\"");
    }

    @Test
    void detail_template_should_wire_metadata_form_cancel_action_and_source_links() throws Exception {
        String template = readResource("templates/accountspayable/debit-memos/detail.html");

        assertThat(template).contains("debit-memo-metadata-form");
        assertThat(template).contains("data-ajax-form=\"true\"");
        assertThat(template).contains("hasAuthority('DEBIT-MEMO_UPDATE-METADATA')");
        assertThat(template).contains("hasAuthority('DEBIT-MEMO_CANCEL')");
        assertThat(template).contains("/metadata");
        assertThat(template).contains("/cancel");
        assertThat(template).contains("/purchasing/purchase-returns/view/{id}");
        assertThat(template).contains("/inventory/goods-issues/{id}");
        assertThat(template).contains("label.debit-memo.allocation-history.empty");
        assertThat(template).contains("label.debit-memo.settlement-recap");
        assertThat(template).contains("supplierMemoNumber");
        assertThat(template).contains("supplierMemoDate");
        assertThat(template).contains("taxDocumentNumber");
        assertThat(template).contains("taxDocumentDate");
        assertThat(template).contains("data-redirect-on-success");
        assertThat(template).contains("DEBIT-MEMO-ALLOCATION_CREATE");
        assertThat(template).contains("/accounts-payable/debit-memo-allocations/create");
        assertThat(template).contains("/accounts-payable/debit-memo-allocations/{id}");
    }

    @Test
    void purchase_return_view_should_link_to_debit_memo_when_present() throws Exception {
        String template = readResource("templates/purchasing/purchase-returns/view.html");

        assertThat(template).contains("debitMemoLink");
        assertThat(template).contains("/accounts-payable/debit-memos/{id}");
        assertThat(template).contains("label.debit-memo.title");
    }

    @Test
    void message_bundles_should_include_debit_memo_keys() {
        ResourceBundle english = ResourceBundle.getBundle("messages", Locale.ENGLISH);
        ResourceBundle indonesian = ResourceBundle.getBundle("messages", Locale.forLanguageTag("id"));

        for (String key : java.util.List.of(
                "label.debit-memo.title",
                "label.debit-memo.metadata",
                "label.debit-memo.settlement-recap",
                "msg.error.debit-memo.supplier-memo-number-duplicate",
                "msg.error.debit-memo.tax-document-number-duplicate",
                "msg.success.debit-memo.metadata-updated"
        )) {
            assertThat(english.containsKey(key)).as(key).isTrue();
            assertThat(indonesian.containsKey(key)).as(key).isTrue();
        }
    }

    private String readResource(String path) throws Exception {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
            assertThat(inputStream).as("Resource not found: %s", path).isNotNull();
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
