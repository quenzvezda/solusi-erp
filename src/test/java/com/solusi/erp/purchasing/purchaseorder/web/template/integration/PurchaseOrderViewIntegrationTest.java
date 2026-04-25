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

    private String readResource(String path) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        assertThat(is).as("Resource not found: %s", path).isNotNull();

        try (InputStream in = is) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
