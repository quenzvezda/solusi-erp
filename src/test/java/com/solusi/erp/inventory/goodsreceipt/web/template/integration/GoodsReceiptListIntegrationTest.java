package com.solusi.erp.inventory.goodsreceipt.web.template.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Goods Receipt List — Template Contract Test")
@Tag("integration-template")
class GoodsReceiptListIntegrationTest {

    private static final String TEMPLATE = "templates/inventory/goods-receipts/list.html";

    @Test
    @DisplayName("list template renders table with goods receipt records")
    void listTemplate_renders() throws Exception {
        String template = readResource(TEMPLATE);

        assertThat(template).contains("goods-receipt");
        assertThat(template).contains("class=\"table");
        assertThat(template).contains("th:each");
    }

    @Test
    @DisplayName("list template renders pagination controls")
    void listTemplate_rendersPaginationControls() throws Exception {
        String template = readResource(TEMPLATE);

        assertThat(template).contains("th:replace=");
        assertThat(template).contains("fragments/table :: pagination");
        assertThat(template).contains("${page}");
    }

    @Test
    @DisplayName("list template is audit-only and uses generic reference columns")
    void listTemplate_isAuditOnlyAndUsesGenericReferenceColumns() throws Exception {
        String template = readResource(TEMPLATE);

        assertThat(template).doesNotContain("/inventory/goods-receipts/create");
        assertThat(template).contains("label.gr.column.referenceType");
        assertThat(template).contains("label.gr.column.referenceCode");
    }

    private String readResource(String path) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        assertThat(is).as("Resource not found: %s", path).isNotNull();

        try (InputStream in = is) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
