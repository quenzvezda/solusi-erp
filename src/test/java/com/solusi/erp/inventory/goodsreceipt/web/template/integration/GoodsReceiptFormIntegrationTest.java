package com.solusi.erp.inventory.goodsreceipt.web.template.integration;

import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptReferenceType;
import com.solusi.erp.inventory.goodsreceipt.web.dto.GoodsReceiptSaveLineRequest;
import com.solusi.erp.inventory.goodsreceipt.web.dto.GoodsReceiptSaveRequest;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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

    @Test
    @DisplayName("create form renders through thymeleaf without parsing errors")
    void createForm_rendersThroughThymeleafWithoutParsingErrors() {
        GoodsReceiptSaveRequest request = new GoodsReceiptSaveRequest();
        request.setReceiptDate(LocalDate.of(2026, 7, 1));
        request.setReferenceType(GoodsReceiptReferenceType.PURCHASE_ORDER);
        request.setReferenceCode("PO-001");
        request.setSupplierName("PT Supplier");
        request.setFacilityName("Main Warehouse");

        assertDoesNotThrow(() -> {
            String html = TemplateTestUtils.renderWithSecurity(
                "inventory/goods-receipts/form",
                Map.of("grRequest", request),
                auth("GOODS-RECEIPT_CREATE")
            );
            assertThat(html).isNotBlank();
        });
    }

    @Test
    @DisplayName("create form renders through thymeleaf with prefilled lines")
    void createForm_rendersThroughThymeleafWithPrefilledLines() {
        GoodsReceiptSaveLineRequest line = new GoodsReceiptSaveLineRequest();
        line.setProductId(10L);
        line.setUomId(20L);
        line.setContainerId(30L);

        GoodsReceiptSaveRequest request = new GoodsReceiptSaveRequest();
        request.setReceiptDate(LocalDate.of(2026, 7, 1));
        request.setReferenceType(GoodsReceiptReferenceType.PURCHASE_ORDER);
        request.setReferenceCode("PO-001");
        request.setSupplierName("PT Supplier");
        request.setFacilityName("Main Warehouse");
        request.setLines(List.of(line));

        assertDoesNotThrow(() -> {
            String html = TemplateTestUtils.renderWithSecurity(
                "inventory/goods-receipts/form",
                Map.of("grRequest", request),
                auth("GOODS-RECEIPT_CREATE")
            );
            assertThat(html).isNotBlank();
        });
    }

    @Test
    @DisplayName("save line request exposes autocomplete display fields for prefilled lines")
    void saveLineRequest_exposesAutocompleteDisplayFields() {
        assertHasGetter("getProductName");
        assertHasGetter("getProductCode");
        assertHasGetter("getUomName");
        assertHasGetter("getUomCode");
        assertHasGetter("getContainerName");
        assertHasGetter("getContainerCode");
    }

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token =
            new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private String readResource(String path) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        assertThat(is).as("Resource not found: %s", path).isNotNull();

        try (InputStream in = is) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void assertHasGetter(String getterName) {
        assertThat(findMethod(getterName))
            .as("GoodsReceiptSaveLineRequest should expose %s for Thymeleaf autocomplete prefill", getterName)
            .isNotNull();
    }

    private Method findMethod(String name) {
        for (Method method : GoodsReceiptSaveLineRequest.class.getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == 0) {
                return method;
            }
        }
        return null;
    }
}
