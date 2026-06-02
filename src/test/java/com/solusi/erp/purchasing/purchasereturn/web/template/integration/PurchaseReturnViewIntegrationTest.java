package com.solusi.erp.purchasing.purchasereturn.web.template.integration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PurchaseReturnViewIntegrationTest {

    private final String html = TemplateFiles.read("templates/purchasing/purchase-returns/view.html");

    @Test
    void view_containsI18nStatusSourceLinksLinesReservationGoodsIssueAndApprovalFragments() {
        assertThat(html).contains("label.purchase-return.status.");
        assertThat(html).contains("/inventory/goods-receipts/{id}");
        assertThat(html).contains("/purchasing/purchase-orders/view/{id}");
        assertThat(html).contains("/inventory/goods-issues/{id}");
        assertThat(html).contains("id=\"table-lines\"");
        assertThat(html).contains("#{label.purchase-return.reservation-summary}");
        assertThat(html).contains("approve-action-banner");
        assertThat(html).contains("approve-reject-panel");
        assertThat(html).contains("approval-history-drawer");
    }

    @Test
    void view_statusActionsUseAjaxPostConfirmationAndAuthorities() {
        assertThat(html).contains("ErpForm.postAction");
        assertThat(html).contains("data-submit-url");
        assertThat(html).contains("data-action-url");
        assertThat(html).contains("data-confirm-message");
        assertThat(html).contains("data-redirect-url");
        assertThat(html).contains("hasAuthority('PURCHASE-RETURN_SUBMIT')");
        assertThat(html).contains("hasAuthority('PURCHASE-RETURN_CONFIRM')");
        assertThat(html).contains("hasAuthority('PURCHASE-RETURN_CANCEL')");
        assertThat(html).doesNotContain("window.confirm");
        assertThat(html).doesNotContain("new bootstrap.Modal");
        PurchaseReturnListIntegrationTest.assertThemeSafe(html);
    }
}
