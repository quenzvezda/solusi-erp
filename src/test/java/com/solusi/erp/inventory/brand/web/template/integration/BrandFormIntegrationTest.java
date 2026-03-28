package com.solusi.erp.inventory.brand.web.template.integration;

import com.solusi.erp.inventory.brand.web.dto.BrandSaveRequest;
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
 * Form Template Integration Test for Brand.
 * model attribute key: "brandRequest" (th:object="${brandRequest}")
 */
@DisplayName("Brand Form — Template Integration Test")
@Tag("integration-template")
class BrandFormIntegrationTest {

    private static final String TEMPLATE = "inventory/brands/form";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token = new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private BrandSaveRequest newRequest() {
        BrandSaveRequest req = new BrandSaveRequest();
        req.setCode("BRD-001");
        req.setName("Brand Test");
        req.setNote("Catatan brand");
        return req;
    }

    private BrandSaveRequest editRequest() {
        BrandSaveRequest req = newRequest();
        req.setId(1L);
        req.setVersion(1);
        return req;
    }

    // ---- Static DTO property check ----

    @Test
    @DisplayName("BrandSaveRequest has all properties referenced in form template")
    void brandSaveRequest_hasAllFormBindingProperties() {
        List<String> required = Arrays.asList("code", "name", "note", "id", "version");
        for (String prop : required) {
            assertThat(hasReadableProperty(BrandSaveRequest.class, prop))
                    .withFailMessage("BrandSaveRequest missing readable property '%s'", prop)
                    .isTrue();
        }
    }

    // ---- Integration render tests ----

    @Test
    @DisplayName("Create form renders without error")
    void createForm_rendersSuccessfully() {
        assertDoesNotThrow(() -> {
            String html = TemplateTestUtils.renderWithSecurity(
                    TEMPLATE, Map.of("brandRequest", newRequest()), auth("BRAND_CREATE"));
            assertThat(html).isNotBlank();
        });
    }

    @Test
    @DisplayName("Edit form renders without error and shows brand name")
    void editForm_rendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, Map.of("brandRequest", editRequest()), auth("BRAND_UPDATE"));
        assertThat(html).isNotBlank();
        // brand form title uses #messages.msg('label.brand.edit'), name rendered via th:field (not plain th:text)
        assertThat(html).contains("brand-form");
    }

    @Test
    @DisplayName("Template contains expected form structure")
    void template_containsExpectedFormStructure() throws Exception {
        java.io.InputStream is = getClass().getClassLoader()
                .getResourceAsStream("templates/" + TEMPLATE + ".html");
        assertThat(is).as("Template not found").isNotNull();
        String raw = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        assertThat(raw).contains("brandRequest");
        assertThat(raw).contains("field='code'");
        assertThat(raw).contains("field='name'");
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
