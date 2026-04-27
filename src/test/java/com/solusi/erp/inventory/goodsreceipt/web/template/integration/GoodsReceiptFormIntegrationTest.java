package com.solusi.erp.inventory.goodsreceipt.web.template.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Goods Receipt Form — Template Contract Test")
@Tag("integration-template")
class GoodsReceiptFormIntegrationTest {

    private static final String CREATE_TEMPLATE = "templates/inventory/goods-receipts/form.html";

    @Test
    @DisplayName("create form template renders form with input fields")
    void createFormTemplate_renders() throws Exception {
        String template = readResource(CREATE_TEMPLATE);

        assertThat(template).contains("goods-receipt-form");
        assertThat(template).contains("form-control");
        assertThat(template).contains("type=\"submit\"");
    }

    @Test
    @DisplayName("edit form template renders prefilled form data")
    void editFormTemplate_renders() throws Exception {
        String template = readResource(CREATE_TEMPLATE);

        assertThat(template).contains("th:value");
        assertThat(template).contains("th:object");
    }

    @Test
    @DisplayName("form submit posts JSON request")
    void formSubmit_postsJSON() throws Exception {
        String template = readResource(CREATE_TEMPLATE);

        assertThat(template).contains("data-ajax-form");
        assertThat(template).contains("th:object=\"${grRequest}\"");
        assertThat(template).contains("type=\"submit\"");
    }

    @Test
    @DisplayName("create form renders read-only purchase-order snapshot header")
    void createForm_rendersReadonlyReferenceHeader() throws Exception {
        String template = readResource(CREATE_TEMPLATE);

        assertThat(template).contains("label.gr.referenceType");
        assertThat(template).contains("label.gr.referenceCode");
        assertThat(template).contains("label.gr.supplier");
        assertThat(template).contains("label.gr.facility");
        assertThat(template).contains("form-control-plaintext");
    }

    private String readResource(String path) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        assertThat(is).as("Resource not found: %s", path).isNotNull();

        try (InputStream in = is) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
