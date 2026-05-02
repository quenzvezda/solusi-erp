package com.solusi.erp.inventory.goodsreceipt.web.template.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Goods Receipt View — Template Contract Test")
@Tag("integration-template")
class GoodsReceiptViewIntegrationTest {

    private static final String TEMPLATE = "templates/inventory/goods-receipts/view.html";

    @Test
    @DisplayName("view template renders detail information")
    void viewTemplate_rendersDetail() throws Exception {
        String template = readResource(TEMPLATE);

        assertThat(template).contains("gr-view-content");
        assertThat(template).contains("${gr");
        assertThat(template).contains("form-control-plaintext");
    }

    @Test
    @DisplayName("view template renders complete button when draft status")
    void viewTemplate_rendersCompleteButton_whenDraft() throws Exception {
        String template = readResource(TEMPLATE);

        assertThat(template).contains("'DRAFT'");
        assertThat(template).contains("complete");
        assertThat(template).contains("btn");
    }

    @Test
    @DisplayName("view template redirects back to detail page after complete")
    void viewTemplate_redirectsBackToDetailPageAfterComplete() throws Exception {
        String template = readResource(TEMPLATE);

        assertThat(template).contains("data-redirect-url");
        assertThat(template).contains("th:data-redirect-url=\"@{'/inventory/goods-receipts/' + ${gr.id}}\"");
    }

    @Test
    @DisplayName("view template renders serial values using bracketed display format")
    void viewTemplate_rendersSerialValuesUsingBracketedDisplayFormat() throws Exception {
        String template = readResource(TEMPLATE);

        assertThat(template).contains("line.serialDisplay");
    }

    @Test
    @DisplayName("view template uses generic reference labels instead of po-only labels")
    void viewTemplate_usesGenericReferenceLabels() throws Exception {
        String template = readResource(TEMPLATE);

        assertThat(template).contains("label.gr.referenceType");
        assertThat(template).contains("label.gr.referenceCode");
        assertThat(template).contains("${gr.referenceCode");
        assertThat(template).doesNotContain("label.gr.poCode");
    }

    private String readResource(String path) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        assertThat(is).as("Resource not found: %s", path).isNotNull();

        try (InputStream in = is) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
