package com.solusi.erp.inventory.uomconversion.web.template.integration;

import com.solusi.erp.inventory.uomconversion.web.dto.UomConversionSaveRequest;
import com.solusi.erp.inventory.uomconversion.web.dto.UomConversionUIInfo;
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
 * Form Template Integration Test for UomConversion.
 * model attribute key: "uomConversionRequest" (th:object="${uomConversionRequest}")
 * Additional model keys: "uoms" (List), "uomUIForm" (UomConversionUIInfo, nullable on create)
 */
@DisplayName("UomConversion Form — Template Integration Test")
@Tag("integration-template")
class UomConversionFormIntegrationTest {

    private static final String TEMPLATE = "inventory/uom-conversions/form";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token = new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private UomConversionSaveRequest newRequest() {
        UomConversionSaveRequest req = new UomConversionSaveRequest();
        req.setProductId(1L);
        req.setFromUomId(1L);
        req.setToUomId(2L);
        req.setConversionFactor(BigDecimal.valueOf(12));
        return req;
    }

    private Map<String, Object> createModel() {
        Map<String, Object> model = new HashMap<>();
        model.put("uomConversionRequest", newRequest());
        model.put("uoms", List.of());
        return model;
    }

    private Map<String, Object> editModel() {
        UomConversionSaveRequest req = newRequest();
        req.setId(1L);
        UomConversionUIInfo ui = new UomConversionUIInfo("Produk A", "PRD-001", "PCS");
        Map<String, Object> model = new HashMap<>();
        model.put("uomConversionRequest", req);
        model.put("uomUIForm", ui);
        model.put("uoms", List.of());
        return model;
    }

    // ---- Static DTO property check ----

    @Test
    @DisplayName("UomConversionSaveRequest has all properties referenced in form template")
    void uomConversionSaveRequest_hasAllFormBindingProperties() {
        List<String> required = Arrays.asList(
                "productId", "fromUomId", "toUomId", "conversionFactor", "id"
        );
        for (String prop : required) {
            assertThat(hasReadableProperty(UomConversionSaveRequest.class, prop))
                    .withFailMessage("UomConversionSaveRequest missing readable property '%s'", prop)
                    .isTrue();
        }
    }

    // ---- Integration render tests ----

    @Test
    @DisplayName("Create form renders without error")
    void createForm_rendersSuccessfully() {
        assertDoesNotThrow(() -> {
            String html = TemplateTestUtils.renderWithSecurity(
                    TEMPLATE, createModel(), auth("UOM-CONVERSION_CREATE"));
            assertThat(html).isNotBlank();
        });
    }

    @Test
    @DisplayName("Edit form with uomUIForm renders without error")
    void editForm_rendersSuccessfully() {
        assertDoesNotThrow(() -> {
            String html = TemplateTestUtils.renderWithSecurity(
                    TEMPLATE, editModel(), auth("UOM-CONVERSION_UPDATE"));
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
        assertThat(raw).contains("uomConversionRequest");
        assertThat(raw).contains("productId");
        assertThat(raw).contains("fromUomId");
        assertThat(raw).contains("conversionFactor");
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
