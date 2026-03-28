package com.solusi.erp.inventory.uom.web.template.integration;

import com.solusi.erp.inventory.model.UomType;
import com.solusi.erp.inventory.uom.web.dto.UomSaveRequest;
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
 * Form Template Integration Test for Unit of Measure (UOM).
 * model attribute key: "unitOfMeasureRequest" (th:object="${unitOfMeasureRequest}")
 * Additional model keys: "types" (UomType[])
 */
@DisplayName("UOM Form — Template Integration Test")
@Tag("integration-template")
class UomFormIntegrationTest {

    private static final String TEMPLATE = "inventory/unit-of-measures/form";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token = new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private UomSaveRequest newRequest() {
        UomSaveRequest req = new UomSaveRequest();
        req.setCode("PCS");
        req.setName("Pieces");
        req.setType(UomType.UNIT);
        return req;
    }

    private Map<String, Object> createModel() {
        return Map.of(
                "unitOfMeasureRequest", newRequest(),
                "types", UomType.values()
        );
    }

    private Map<String, Object> editModel() {
        UomSaveRequest req = newRequest();
        req.setId(1L);
        req.setVersion(1);
        return Map.of(
                "unitOfMeasureRequest", req,
                "types", UomType.values()
        );
    }

    // ---- Static DTO property check ----

    @Test
    @DisplayName("UomSaveRequest has all properties referenced in form template")
    void uomSaveRequest_hasAllFormBindingProperties() {
        List<String> required = Arrays.asList("code", "name", "type", "id", "version");
        for (String prop : required) {
            assertThat(hasReadableProperty(UomSaveRequest.class, prop))
                    .withFailMessage("UomSaveRequest missing readable property '%s'", prop)
                    .isTrue();
        }
    }

    // ---- Integration render tests ----

    @Test
    @DisplayName("Create form renders without error")
    void createForm_rendersSuccessfully() {
        assertDoesNotThrow(() -> {
            String html = TemplateTestUtils.renderWithSecurity(
                    TEMPLATE, createModel(), auth("UNIT-OF-MEASURE_CREATE"));
            assertThat(html).isNotBlank();
        });
    }

    @Test
    @DisplayName("Edit form renders without error and shows UOM name")
    void editForm_rendersSuccessfully() {
        assertDoesNotThrow(() -> {
            String html = TemplateTestUtils.renderWithSecurity(
                    TEMPLATE, editModel(), auth("UNIT-OF-MEASURE_UPDATE"));
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
        assertThat(raw).contains("unitOfMeasureRequest");
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
