package com.solusi.erp.purchasing.purchaseorder.web.template.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Purchase Order View — Template Contract Test")
@Tag("integration-template")
class PurchaseOrderViewIntegrationTest {

    private static final String TEMPLATE = "templates/purchasing/purchase-orders/view.html";

    @Test
    @DisplayName("approval status lives in the main detail card without a separate approval info card")
    void approvalStatus_livesInMainDetailCardWithoutSeparateApprovalInfoCard() throws Exception {
        String template = readResource(TEMPLATE);

        assertThat(template).contains("th:if=\"${approvalRequestId != null}\" class=\"col-md-4\"");
        assertThat(template).contains("th:text=\"#{label.approval.status}\"");
        assertThat(template).contains("class=\"btn btn-white\"");
        assertThat(template).doesNotContain("th:text=\"#{label.approval.info}\"");
        assertThat(template).doesNotContain("class=\"btn btn-white w-100\"");
        assertThat(template).doesNotContain("th:classappend=\"${approvalRequestId != null} ? 'col-lg-8' : 'col-12'\"");
    }

    @Test
    @DisplayName("po detail template renders goods receipt buttons when can receive")
    void poDetailTemplate_rendersGoodsReceiptButtons_whenCanReceive() throws Exception {
        String template = readResource(TEMPLATE);

        assertThat(template).contains("th:if=\"${canCreateGoodsReceipt}\"");
        assertThat(template).contains("#{label.gr.action.create}");
        assertThat(template).contains("/inventory/goods-receipts/create");
        assertThat(template).contains("poId=${po.id}");
        assertThat(template).contains("goodsReceiptCount");
        assertThat(template).contains("hasAuthority('GOODS-RECEIPT_CREATE')");
        assertThat(template).contains("hasAuthority('GOODS-RECEIPT_READ')");
    }

    @Test
    @DisplayName("po detail template renders send button for approved purchase orders")
    void poDetailTemplate_rendersSendButtonForApprovedPurchaseOrders() throws Exception {
        String template = readResource(TEMPLATE);

        assertThat(template).contains("canSendPurchaseOrder");
        assertThat(template).contains("#{label.po.action.send}");
        assertThat(template).contains("hasAuthority('PO_SEND')");
        assertThat(template).contains("data-send-url");
    }

    @Test
    @DisplayName("po detail send button requires confirmation modal message")
    void poDetailTemplate_sendButtonRequiresConfirmationMessage() throws Exception {
        String template = readResource(TEMPLATE);

        assertThat(template).contains("data-confirm-message");
        assertThat(template).contains("#{msg.confirm.po.send}");
    }

    @Test
    @DisplayName("goods receipt create action keys exist in both locales")
    void goodsReceiptCreateActionKeys_existInBothLocales() throws Exception {
        String messagesEn = readResource("messages.properties");
        String messagesId = readResource("messages_id.properties");

        assertThat(messagesEn).contains("label.gr.action.create=");
        assertThat(messagesId).contains("label.gr.action.create=");
        assertThat(messagesEn).contains("label.gr.list.title=");
        assertThat(messagesId).contains("label.gr.list.title=");
    }

    private String readResource(String path) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        assertThat(is).as("Resource not found: %s", path).isNotNull();

        try (InputStream in = is) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
