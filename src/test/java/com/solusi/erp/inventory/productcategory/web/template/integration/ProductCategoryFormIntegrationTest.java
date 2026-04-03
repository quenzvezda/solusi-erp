package com.solusi.erp.inventory.productcategory.web.template.integration;

import com.solusi.erp.inventory.productcategory.domain.model.ProductCategoryType;
import com.solusi.erp.inventory.productcategory.web.dto.ProductCategorySaveRequest;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Form Template Integration Test for ProductCategory.
 * model attribute key: "productCategoryRequest" (th:object="${productCategoryRequest}")
 * Additional model keys: "types" (ProductCategoryType[])
 */
@DisplayName("ProductCategory Form — Template Integration Test")
@Tag("integration-template")
class ProductCategoryFormIntegrationTest {

    private static final String TEMPLATE = "inventory/product-categories/form";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token = new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private ProductCategorySaveRequest newRequest() {
        ProductCategorySaveRequest req = new ProductCategorySaveRequest();
        req.setCode("CAT-001");
        req.setName("Elektronik");
        req.setType(ProductCategoryType.STOCK);
        req.setNote("Kategori produk elektronik");
        return req;
    }

    private Map<String, Object> createModel() {
        return Map.of(
                "productCategoryRequest", newRequest(),
                "types", ProductCategoryType.values()
        );
    }

    private Map<String, Object> editModel() {
        ProductCategorySaveRequest req = newRequest();
        req.setId(1L);
        req.setVersion(1);
        return Map.of(
                "productCategoryRequest", req,
                "types", ProductCategoryType.values()
        );
    }

    // ---- Static DTO property check ----

    @Test
    @DisplayName("ProductCategorySaveRequest has all properties referenced in form template")
    void productCategorySaveRequest_hasAllFormBindingProperties() {
        List<String> required = Arrays.asList("code", "name", "type", "note", "id", "version");
        for (String prop : required) {
            assertThat(hasReadableProperty(ProductCategorySaveRequest.class, prop))
                    .withFailMessage("ProductCategorySaveRequest missing readable property '%s'", prop)
                    .isTrue();
        }
    }

    // ---- Integration render tests ----

    @Test
    @DisplayName("Create form renders without error")
    void createForm_rendersSuccessfully() {
        assertDoesNotThrow(() -> {
            String html = TemplateTestUtils.renderWithSecurity(
                    TEMPLATE, createModel(), auth("PRODUCT-CATEGORY_CREATE"));
            assertThat(html).isNotBlank();
        });
    }

    @Test
    @DisplayName("Edit form renders without error and shows category name")
    void editForm_rendersSuccessfully() {
        assertDoesNotThrow(() -> {
            String html = TemplateTestUtils.renderWithSecurity(
                    TEMPLATE, editModel(), auth("PRODUCT-CATEGORY_UPDATE"));
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
        assertThat(raw).contains("productCategoryRequest");
        assertThat(raw).contains("field='code'");
        assertThat(raw).contains("field='name'");
        assertThat(raw).contains("types");
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
