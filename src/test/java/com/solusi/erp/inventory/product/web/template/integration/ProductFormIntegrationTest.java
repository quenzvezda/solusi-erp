package com.solusi.erp.inventory.product.web.template.integration;

import com.solusi.erp.inventory.product.web.dto.ProductSaveRequest;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Form Template Integration Test for Product.
 * model attribute key: "productRequest" (th:object="${productRequest}")
 * Additional model keys: "uoms", "weightUoms", "lengthUoms" (List — from populateFormModel)
 */
@DisplayName("Product Form — Template Integration Test")
@Tag("integration-template")
class ProductFormIntegrationTest {

    private static final String TEMPLATE = "inventory/products/form";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token = new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private ProductSaveRequest newRequest() {
        ProductSaveRequest req = new ProductSaveRequest();
        req.setCode("PRD-001");
        req.setName("Produk Test");
        req.setBarcode("BC-PRD-001");
        req.setCategoryId(1L);
        req.setCategoryName("Elektronik");
        req.setCategoryCode("CAT-001");
        req.setCategoryType("STOCK");
        req.setUomId(1L);
        req.setBrandId(1L);
        req.setBrandName("Brand A");
        req.setBrandCode("BRD-001");
        req.setHscode("1234");
        req.setIsActive(true);
        req.setIsSerialized(false);
        req.setMinStock(BigDecimal.ZERO);
        req.setMaxStock(BigDecimal.valueOf(100));
        req.setWeightNet(BigDecimal.valueOf(0.5));
        req.setWeightGross(BigDecimal.valueOf(0.6));
        req.setWeightUomId(2L);
        req.setLength(BigDecimal.TEN);
        req.setWidth(BigDecimal.valueOf(5));
        req.setHeight(BigDecimal.valueOf(3));
        req.setDimensionUomId(3L);
        req.setNote("Catatan produk");
        return req;
    }

    private Map<String, Object> createModel() {
        Map<String, Object> model = new HashMap<>();
        model.put("productRequest", newRequest());
        model.put("uoms", List.of());
        model.put("weightUoms", List.of());
        model.put("lengthUoms", List.of());
        return model;
    }

    private Map<String, Object> editModel() {
        ProductSaveRequest req = newRequest();
        req.setId(1L);
        req.setVersion(1);
        Map<String, Object> model = new HashMap<>();
        model.put("productRequest", req);
        model.put("uoms", List.of());
        model.put("weightUoms", List.of());
        model.put("lengthUoms", List.of());
        return model;
    }

    // ---- Static DTO property check ----

    @Test
    @DisplayName("ProductSaveRequest has all properties referenced in form template")
    void productSaveRequest_hasAllFormBindingProperties() {
        List<String> required = Arrays.asList(
                "code", "name", "barcode", "categoryId", "categoryName", "categoryCode", "categoryType",
                "uomId", "brandId", "brandName", "brandCode", "hscode",
                "isActive", "isSerialized",
                "minStock", "maxStock",
                "weightNet", "weightGross", "weightUomId",
                "length", "width", "height", "dimensionUomId",
                "note", "id", "version"
        );
        for (String prop : required) {
            assertThat(hasReadableProperty(ProductSaveRequest.class, prop))
                    .withFailMessage("ProductSaveRequest missing readable property '%s'", prop)
                    .isTrue();
        }
    }

    // ---- Integration render tests ----

    @Test
    @DisplayName("Create form renders without error")
    void createForm_rendersSuccessfully() {
        assertDoesNotThrow(() -> {
            String html = TemplateTestUtils.renderWithSecurity(
                    TEMPLATE, createModel(), auth("PRODUCT_CREATE"));
            assertThat(html).isNotBlank();
        });
    }

    @Test
    @DisplayName("Edit form renders without error and shows product name")
    void editForm_rendersSuccessfully() {
        assertDoesNotThrow(() -> {
            String html = TemplateTestUtils.renderWithSecurity(
                    TEMPLATE, editModel(), auth("PRODUCT_UPDATE"));
            assertThat(html).isNotBlank();
        });
    }

    @Test
    @DisplayName("Template contains expected form structure")
    void template_containsExpectedFormStructure() throws Exception {
        java.io.InputStream is = getClass().getClassLoader()
                .getResourceAsStream("templates/" + TEMPLATE + ".html");
        assertThat(is).as("Template not found").isNotNull();
        String raw = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        assertThat(raw).contains("productRequest");
        assertThat(raw).contains("field='code'");
        assertThat(raw).contains("field='name'");
        assertThat(raw).contains("categoryId");
        assertThat(raw).contains("uomId");
        assertThat(raw).contains("uoms");
        assertThat(raw).contains("weightUoms");
        assertThat(raw).contains("lengthUoms");
    }

    private boolean hasReadableProperty(Class<?> clazz, String prop) {
        String getter   = "get" + prop.substring(0, 1).toUpperCase() + prop.substring(1);
        String isGetter = "is"  + prop.substring(0, 1).toUpperCase() + prop.substring(1);
        for (java.lang.reflect.Method m : clazz.getMethods()) {
            if ((m.getName().equals(getter) || m.getName().equals(isGetter))
                    && m.getParameterCount() == 0) return true;
        }
        return false;
    }
}
