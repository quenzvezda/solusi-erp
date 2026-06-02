package com.solusi.erp.purchasing.purchasereturn.web.template.integration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PurchaseReturnSelectSourceIntegrationTest {

    private final String html = TemplateFiles.read("templates/purchasing/purchase-returns/select-source.html");

    @Test
    void selectSource_isRegularPageWithCompactFiltersTrinityAutocompleteAndDatePickers() {
        assertThat(html).contains("purchase-return-source-content");
        assertThat(html).contains("data-lookup-path=\"parties\"");
        assertThat(html).contains("data-lookup-path=\"purchasing/purchase-return-source-pos\"");
        assertThat(html).contains("th:value=\"${supplierLookup.id}\"");
        assertThat(html).contains("th:text=\"${supplierLookup.name}\"");
        assertThat(html).contains("data-subtext=${supplierLookup.subText}");
        assertThat(html).contains("name=\"receiptDateFrom\"");
        assertThat(html).contains("name=\"receiptDateTo\"");
        assertThat(html).contains("data-picker=\"date\"");
    }

    @Test
    void selectSource_linksGoodsReceiptPurchaseOrderAndContinuesWithOneReceipt() {
        assertThat(html).contains("/inventory/goods-receipts/{id}");
        assertThat(html).contains("/purchasing/purchase-orders/view/{id}");
        assertThat(html).contains("/purchasing/purchase-returns/create-from-reference(goodsReceiptId=${row.goodsReceiptId})");
        assertThat(html).contains("fragments/table :: pagination(${page})");
        PurchaseReturnListIntegrationTest.assertThemeSafe(html);
    }
}
