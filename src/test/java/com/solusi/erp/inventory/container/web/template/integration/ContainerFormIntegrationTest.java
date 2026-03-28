package com.solusi.erp.inventory.container.web.template.integration;

import com.solusi.erp.inventory.container.web.dto.ContainerSaveRequest;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Form Template Integration Test for Container.
 * model attribute key: "containerRequest" (th:object="${containerRequest}")
 * Additional model keys: "containerUI" (Map, nullable on create)
 */
@DisplayName("Container Form — Template Integration Test")
@Tag("integration-template")
class ContainerFormIntegrationTest {

    private static final String TEMPLATE = "inventory/containers/form";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token = new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private ContainerSaveRequest newRequest() {
        ContainerSaveRequest req = new ContainerSaveRequest();
        req.setGridId(1L);
        req.setCode("CON-001");
        req.setName("Rak A1");
        req.setBarcode("BC-001");
        req.setLength(BigDecimal.valueOf(100));
        req.setWidth(BigDecimal.valueOf(50));
        req.setHeight(BigDecimal.valueOf(200));
        req.setMaxWeight(BigDecimal.valueOf(500));
        req.setNote("Test container");
        req.setIsActive(true);
        return req;
    }

    private Map<String, Object> createModel() {
        return Map.of("containerRequest", newRequest());
    }

    private Map<String, Object> editModel() {
        ContainerSaveRequest req = newRequest();
        req.setId(1L);
        req.setVersion(1);
        Map<String, Object> ui = Map.of("gridName", "Grid A", "gridCode", "GRD-001");
        return Map.of("containerRequest", req, "containerUI", ui);
    }

    // ---- Static DTO property check ----

    @Test
    @DisplayName("ContainerSaveRequest has all properties referenced in form template")
    void containerSaveRequest_hasAllFormBindingProperties() {
        List<String> required = Arrays.asList(
                "gridId", "code", "name", "barcode",
                "length", "width", "height", "maxWeight",
                "isActive", "note", "id", "version"
        );
        for (String prop : required) {
            assertThat(hasReadableProperty(ContainerSaveRequest.class, prop))
                    .withFailMessage("ContainerSaveRequest missing readable property '%s'", prop)
                    .isTrue();
        }
    }

    // ---- Integration render tests ----

    @Test
    @DisplayName("Create form renders without error")
    void createForm_rendersSuccessfully() {
        assertDoesNotThrow(() -> {
            String html = TemplateTestUtils.renderWithSecurity(
                    TEMPLATE, createModel(), auth("CONTAINER_CREATE"));
            assertThat(html).isNotBlank();
        });
    }

    @Test
    @DisplayName("Edit form renders without error and shows container name")
    void editForm_rendersSuccessfully() {
        assertDoesNotThrow(() -> {
            String html = TemplateTestUtils.renderWithSecurity(
                    TEMPLATE, editModel(), auth("CONTAINER_UPDATE"));
            assertThat(html).isNotBlank();
            assertThat(html).contains("Rak A1");
        });
    }

    @Test
    @DisplayName("Template contains expected form structure")
    void template_containsExpectedFormStructure() throws Exception {
        java.io.InputStream is = getClass().getClassLoader()
                .getResourceAsStream("templates/" + TEMPLATE + ".html");
        assertThat(is).as("Template not found").isNotNull();
        String raw = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        assertThat(raw).contains("containerRequest");
        assertThat(raw).contains("field='code'");
        assertThat(raw).contains("field='name'");
        assertThat(raw).contains("field='gridId'");
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
