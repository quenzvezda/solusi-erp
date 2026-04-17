package com.solusi.erp.purchasing.purchaserequisition.web.template.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Purchase Requisition Form — Template Contract Test")
@Tag("integration-template")
class PurchaseRequisitionFormIntegrationTest {

    private static final String TEMPLATE = "templates/purchasing/purchase-requisitions/form.html";

    @Test
    @DisplayName("Edit form uses ternary guards for prUI autocomplete values")
    void editFormUsesTernaryGuardsForPrUiValues() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(TEMPLATE);
        assertThat(is).as("Template not found: %s", TEMPLATE).isNotNull();

        String raw;
        try (InputStream in = is) {
            raw = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        assertThat(raw).doesNotContain("prUI?.requesterText");
        assertThat(raw).doesNotContain("prUI?.facilityText");
        assertThat(raw).doesNotContain("prUI?.supplierText");
        assertThat(raw).contains("prUI != null ? prUI['requesterText'] : ''");
        assertThat(raw).contains("prUI != null ? prUI['facilityText'] : ''");
        assertThat(raw).contains("prUI != null ? prUI['supplierText'] : ''");
    }

    @Test
    @DisplayName("Date picker helper skips the hidden row template source")
    void datePickerHelperSkipsHiddenRowTemplateSource() throws Exception {
        String script = readResource("static/js/shared/erp-datetime-picker.js");

        assertThat(script).contains("row-template-source");
        assertThat(script).contains("isInsideTemplateSource");
        assertThat(script).contains("if (isInsideTemplateSource(input)) return;");
    }

    private String readResource(String path) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        assertThat(is).as("Resource not found: %s", path).isNotNull();

        try (InputStream in = is) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
