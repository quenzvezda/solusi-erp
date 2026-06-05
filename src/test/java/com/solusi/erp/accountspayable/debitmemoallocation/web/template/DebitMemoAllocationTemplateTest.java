package com.solusi.erp.accountspayable.debitmemoallocation.web.template;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class DebitMemoAllocationTemplateTest {

    @Test
    void list_detail_form_and_fragments_should_wire_expected_ids_routes_and_permissions() throws Exception {
        String list = readResource("templates/accountspayable/debit-memo-allocations/list.html");
        String detail = readResource("templates/accountspayable/debit-memo-allocations/detail.html");
        String form = readResource("templates/accountspayable/debit-memo-allocations/form.html");
        String vendorBillSelector = readResource("templates/accountspayable/debit-memo-allocations/fragments/vendor-bill-selector.html");
        String debitMemoSelector = readResource("templates/accountspayable/debit-memo-allocations/fragments/debit-memo-selector.html");
        String script = readResource("static/js/accountspayable/debit-memo-allocations/form.js");

        assertThat(list).contains("DEBIT-MEMO-ALLOCATION_CREATE", "DEBIT-MEMO-ALLOCATION_UPDATE", "DEBIT-MEMO-ALLOCATION_CANCEL");
        assertThat(detail).contains("DEBIT-MEMO-ALLOCATION_CONFIRM", "DEBIT-MEMO-ALLOCATION_REVERSE", "ErpForm.postAction",
                "dma-reverse-form", "data-bs-target=\"#dma-reverse-modal\"");
        assertThat(form).contains("data-ajax-form=\"true\"", "data-picker=\"date\"", "debit-memo-selector-body",
                "vendor-bill-selector-body", "dma-lines-body", "erp-number-decimal");
        assertThat(vendorBillSelector).contains("id=\"vendor-bill-selector-body\"", "js-dma-vendor-bill-option", "data-outstanding");
        assertThat(debitMemoSelector).contains("id=\"debit-memo-selector-body\"", "js-dma-debit-memo-option", "data-remaining");
        assertThat(script).contains("ERP.ModalSelector.open", "js-dma-remove-line", "initNumericInputs", "erp-number-decimal", "lines[");
    }

    private String readResource(String path) throws Exception {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
            assertThat(inputStream).as("Resource not found: %s", path).isNotNull();
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
