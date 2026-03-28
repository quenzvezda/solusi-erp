package com.solusi.erp.inventory.grid.web.template.integration;

import com.solusi.erp.inventory.grid.web.dto.GridSaveRequest;
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
 * Form Template Integration Test for Grid.
 * model attribute key: "gridRequest" (th:object="${gridRequest}")
 * Additional model keys: "facilities" (List — from populateFacilities)
 */
@DisplayName("Grid Form — Template Integration Test")
@Tag("integration-template")
class GridFormIntegrationTest {

    private static final String TEMPLATE = "inventory/grids/form";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token = new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private GridSaveRequest newRequest() {
        GridSaveRequest req = new GridSaveRequest();
        req.setFacilityId(1L);
        req.setCode("GRD-A1");
        req.setName("Grid A1");
        req.setNote("Baris pertama");
        req.setIsActive(true);
        return req;
    }

    private Map<String, Object> createModel() {
        return Map.of(
                "gridRequest", newRequest(),
                "facilities", List.of()
        );
    }

    private Map<String, Object> editModel() {
        GridSaveRequest req = newRequest();
        req.setId(1L);
        req.setVersion(1);
        return Map.of(
                "gridRequest", req,
                "facilities", List.of()
        );
    }

    // ---- Static DTO property check ----

    @Test
    @DisplayName("GridSaveRequest has all properties referenced in form template")
    void gridSaveRequest_hasAllFormBindingProperties() {
        List<String> required = Arrays.asList(
                "facilityId", "code", "name", "isActive", "note", "id", "version"
        );
        for (String prop : required) {
            assertThat(hasReadableProperty(GridSaveRequest.class, prop))
                    .withFailMessage("GridSaveRequest missing readable property '%s'", prop)
                    .isTrue();
        }
    }

    // ---- Integration render tests ----

    @Test
    @DisplayName("Create form renders without error")
    void createForm_rendersSuccessfully() {
        assertDoesNotThrow(() -> {
            String html = TemplateTestUtils.renderWithSecurity(
                    TEMPLATE, createModel(), auth("GRID_CREATE"));
            assertThat(html).isNotBlank();
        });
    }

    @Test
    @DisplayName("Edit form renders without error and shows grid name")
    void editForm_rendersSuccessfully() {
        assertDoesNotThrow(() -> {
            String html = TemplateTestUtils.renderWithSecurity(
                    TEMPLATE, editModel(), auth("GRID_UPDATE"));
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
        assertThat(raw).contains("gridRequest");
        assertThat(raw).contains("field='code'");
        assertThat(raw).contains("field='name'");
        assertThat(raw).contains("facilities");
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
