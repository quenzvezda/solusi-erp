package com.solusi.erp.purchasing.purchasereturn.web.template.integration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PurchaseReturnFormIntegrationTest {

    private final String html = TemplateFiles.read("templates/purchasing/purchase-returns/form.html");
    private final String js = TemplateFiles.read("static/js/purchasing/purchase-return/form.js");
    private final String grSelector = TemplateFiles.read("templates/purchasing/purchase-returns/fragments/gr-line-selector-modal.html");
    private final String serialSelector = TemplateFiles.read("templates/purchasing/purchase-returns/fragments/serial-selector-modal.html");

    @Test
    void form_containsAjaxDateSnapshotSummaryDynamicRowsAndAdaptiveThemeContract() {
        assertThat(html).contains("layout(~{:: .purchase-return-form-content}, ~{:: #page-specific-scripts})");
        assertThat(html).contains("data-ajax-form=\"true\"");
        assertThat(html).contains("data-redirect-on-success=\"/purchasing/purchase-returns\"");
        assertThat(html).contains("class=\"alert-container\"");
        assertThat(html).contains("id=\"loading-indicator\"");
        assertThat(html).contains("data-picker=\"date\"");
        assertThat(html).contains("id=\"row-template-source\"");
        assertThat(html).contains("id=\"summary-total-qty\"");
        assertThat(html).contains("erp-number-decimal");
        assertThat(html).doesNotContain("type=\"number\"");
        assertThat(html).contains("#messages.msg(reason.messageKey)");
        assertThat(html).doesNotContain("th:text=\"${reason}\"");
        PurchaseReturnListIntegrationTest.assertThemeSafe(html);
    }

    @Test
    void form_andFragments_keepStableModalRootSearchPaginationAndPayloadContracts() {
        assertThat(html).contains("'purchase-return-gr-line-selector-results'");
        assertThat(html).contains("'purchase-return-serial-selector-results'");
        assertThat(grSelector).contains("id=\"purchase-return-gr-line-selector-results\"");
        assertThat(grSelector).contains("th:fragment=\"purchase-return-gr-line-selector-results\"");
        assertThat(grSelector).contains("hx-target=\"#purchase-return-gr-line-selector-results\"");
        assertThat(grSelector).contains("fragments/table :: pagination(${page})");
        assertThat(serialSelector).contains("id=\"purchase-return-serial-selector-results\"");
        assertThat(serialSelector).contains("th:fragment=\"purchase-return-serial-selector-results\"");
        assertThat(serialSelector).contains("hx-target=\"#purchase-return-serial-selector-results\"");
        assertThat(serialSelector).contains("fragments/table :: pagination(${page})");
    }

    @Test
    void selectors_exposeSelectionAndActualInventoryTrinityPayloadOnlyThroughDataAttributes() {
        assertThat(grSelector).contains("data-selection-key=${row.selectionKey}");
        assertThat(grSelector).contains("data-goods-receipt-line-id=${row.goodsReceiptLineId}");
        assertThat(grSelector).contains("data-product-id=${row.productId}", "data-product-name=${row.productName}", "data-product-subtext=${row.productCode}");
        assertThat(grSelector).contains("data-uom-id=${row.uomId}", "data-uom-name=${row.uomName}", "data-uom-subtext=${row.uomCode}");
        assertThat(grSelector).contains("data-facility-id=${row.facilityId}", "data-grid-name=${row.gridName}", "data-container-name=${row.containerName}");
        assertThat(grSelector).contains("data-outstanding-quantity=${row.outstandingQuantity}");
        assertThat(grSelector).contains("data-valuation-reference-type=${row.valuationReferenceType}");
        assertThat(serialSelector).contains("data-serial-number=${row.serialNumber}");
    }

    @Test
    void pageScript_usesRequiredHelpersCaptureValidationGroupingAndNoDirectBootstrapModal() {
        assertThat(js).contains("new ErpLineManager");
        assertThat(js).contains("ErpNumeric.get");
        assertThat(js).contains("ErpNumeric.set");
        assertThat(js).contains("window.ERP.ModalSelector.open");
        assertThat(js).contains("window.ERP.ModalSelector.close");
        assertThat(js).contains("ErpModal.showWarning");
        assertThat(js).contains("addEventListener('submit', validateBeforeSubmit, true)");
        assertThat(js).contains("beforeunload");
        assertThat(js).contains("findSerializedRow");
        assertThat(js).contains("selectedSerialKeys");
        assertThat(js).doesNotContain("window.confirm");
        assertThat(js).doesNotContain("new bootstrap.Modal");
    }
}
