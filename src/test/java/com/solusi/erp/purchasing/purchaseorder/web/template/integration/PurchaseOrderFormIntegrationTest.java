package com.solusi.erp.purchasing.purchaseorder.web.template.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Purchase Order Form — Template Contract Test")
@Tag("integration-template")
class PurchaseOrderFormIntegrationTest {

    @Test
    @DisplayName("STANDARD flow uses modal selector trigger and hidden prId instead of plain select")
    void standardFlow_usesModalSelectorTriggerAndHiddenPrId() throws Exception {
        String template = readResource("templates/purchasing/purchase-orders/form.html");
        String script = readResource("static/js/purchasing/purchase-order-form.js");
        String selectorHelper = readResource("static/js/shared/erp-modal-selector.js");

        assertThat(template).contains("id=\"input-pr-id\"");
        assertThat(template).contains("type=\"hidden\"");
        assertThat(template).contains("id=\"btn-select-pr\"");
        assertThat(template).contains("id=\"btn-clear-pr\"");
        assertThat(template).contains("modal-po-pr-selector");
        assertThat(template).contains("modal-po-pr-line-selector");
        assertThat(template).contains(".prLineId");
        assertThat(template).doesNotContain("id=\"select-pr-reference\"");

        assertThat(script).contains("clearStandardSelection");
        assertThat(script).contains("syncPrFieldState");
        assertThat(script).contains("openPrSelector");
        assertThat(script).contains("openPrLineSelector");
        assertThat(script).contains("setLookupValue");

        assertThat(selectorHelper).contains("window.ERP.ModalSelector");
        assertThat(selectorHelper).contains("open: function");
        assertThat(selectorHelper).contains("close: function");
    }

    @Test
    @DisplayName("selector helper supports bootstrap and tabler modal providers")
    void selectorHelper_supportsBootstrapAndTablerModalProviders() throws Exception {
        String selectorHelper = readResource("static/js/shared/erp-modal-selector.js");

        assertThat(selectorHelper).contains("window.bootstrap || window.tabler");
    }

    @Test
    @DisplayName("selector helper closes existing bootstrap instance instead of creating a new one")
    void selectorHelper_closesExistingModalInstance() throws Exception {
        String selectorHelper = readResource("static/js/shared/erp-modal-selector.js");

        assertThat(selectorHelper).contains("bootstrap.Modal.getInstance(modalEl)");
    }

    @Test
    @DisplayName("PR line selector formats visible decimals with two fraction digits")
    void prLineSelector_formatsVisibleDecimalsWithTwoFractionDigits() throws Exception {
        String template = readResource("templates/purchasing/purchase-orders/fragments/pr-line-selector-modal.html");

        assertThat(template).contains("#numbers.formatDecimal(row.requestedQuantity, 1, 'COMMA', 2, 'POINT')");
        assertThat(template).contains("#numbers.formatDecimal(row.remainingQuantity, 1, 'COMMA', 2, 'POINT')");
        assertThat(template).contains("#numbers.formatDecimal(row.estimatedUnitPrice, 1, 'COMMA', 2, 'POINT')");
    }

    @Test
    @DisplayName("preedit flow reuses locked PO type and PR display instead of PR hash fallback")
    void preeditFlow_reusesLockedPoTypeAndPrDisplay() throws Exception {
        String template = readResource("templates/purchasing/purchase-orders/form.html");

        assertThat(template).contains("th:disabled=\"${poRequest.id != null}\"");
        assertThat(template).contains("poUI != null ? poUI['prDisplay'] : ''");
        assertThat(template).doesNotContain("'PR #' + poRequest.prId");
    }

    @Test
    @DisplayName("preedit standard flow locks derived header lookups on initialization")
    void preeditStandardFlow_locksDerivedHeaderLookupsOnInitialization() throws Exception {
        String script = readResource("static/js/purchasing/purchase-order-form.js");

        assertThat(script).contains("if (!config.isNew && getSelectedPoType() === 'STANDARD' && prIdInput && prIdInput.value)");
        assertThat(script).contains("lockLookup(supplierSelect)");
        assertThat(script).contains("lockLookup(facilitySelect)");
        assertThat(script).contains("lockLookup(currencySelect)");
    }

    @Test
    @DisplayName("standard add line uses PR line selector in preedit too")
    void standardAddLine_usesPrLineSelectorInPreeditToo() throws Exception {
        String template = readResource("templates/purchasing/purchase-orders/form.html");
        String script = readResource("static/js/purchasing/purchase-order-form.js");

        assertThat(template).contains("th:if=\"${poRequest.id == null || poRequest.prId != null}\"");
        assertThat(script).contains("function openPrLineSelector() {");
        assertThat(script).contains("!document.getElementById('modal-po-pr-line-selector')");
        assertThat(script).contains("if (getSelectedPoType() === 'STANDARD') {");
        assertThat(script).contains("openPrLineSelector();");
        assertThat(script).contains("addManualLineRow();");
    }

    @Test
    @DisplayName("standard add line warns when PR line selector modal shell is unavailable")
    void standardAddLine_warnsWhenPrLineSelectorModalShellIsUnavailable() throws Exception {
        String script = readResource("static/js/purchasing/purchase-order-form.js");

        assertThat(script).contains("document.getElementById('modal-po-pr-line-selector')");
        assertThat(script).contains("warn(config.prLineSelectorUnavailable)");
    }

    @Test
    @DisplayName("standard quantity uses max attribute and clamps back to PR remaining quantity")
    void standardQuantity_usesMaxAttributeAndClampsToRemainingQuantity() throws Exception {
        String template = readResource("templates/purchasing/purchase-orders/form.html");
        String script = readResource("static/js/purchasing/purchase-order-form.js");

        assertThat(template).contains("th:attr=\"max=${line.maxQuantity}\"");
        assertThat(script).contains("qtyInput.getAttribute('max')");
        assertThat(script).contains("setNumericInputValue(qtyInput, maxQty)");
    }

    @Test
    @DisplayName("line item table no longer renders drawer pencil action")
    void lineItemTable_noLongerRendersDrawerPencilAction() throws Exception {
        String template = readResource("templates/purchasing/purchase-orders/form.html");

        assertThat(template).doesNotContain("btn-edit-line");
    }

    private String readResource(String path) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        assertThat(is).as("Resource not found: %s", path).isNotNull();

        try (InputStream in = is) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
