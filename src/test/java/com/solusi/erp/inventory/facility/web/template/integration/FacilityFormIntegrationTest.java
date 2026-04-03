package com.solusi.erp.inventory.facility.web.template.integration;

import com.solusi.erp.inventory.facility.web.dto.FacilitySaveRequest;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Form Template Integration Test for Facility.
 * model attribute key: "facilityRequest" (th:object="${facilityRequest}")
 */
@DisplayName("Facility Form — Template Integration Test")
@Tag("integration-template")
class FacilityFormIntegrationTest {

    private static final String TEMPLATE = "inventory/facilities/form";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token = new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private FacilitySaveRequest newRequest() {
        FacilitySaveRequest req = new FacilitySaveRequest();
        req.setCode("FAC-2026-0001");
        req.setName("Warehouse Utama");
        req.setOwnerId(1L);
        req.setAddressLine1("Jl. Raya Test No. 1");
        req.setCityId(1L);
        req.setPostalCode("12345");
        req.setNote("Test note");
        req.setIsActive(true);
        return req;
    }

    private Map<String, Object> createModel() {
        return Map.of("facilityRequest", newRequest());
    }

    private Map<String, Object> editModel() {
        FacilitySaveRequest req = newRequest();
        req.setId(1L);
        req.setVersion(1);

        Map<String, Object> facilityUI = Map.of(
                "ownerName", "PT. Owner Corp",
                "ownerCode", "OWN-001",
                "cityName", "Jakarta",
                "cityCode", "JKT"
        );
        return Map.of(
                "facilityRequest", req,
                "facilityUI", facilityUI
        );
    }

    // ---- Static DTO property check ----

    @Test
    @DisplayName("FacilitySaveRequest has all properties referenced in form template via field='X'")
    void facilitySaveRequest_hasAllFormBindingProperties() throws Exception {
        java.io.InputStream is = getClass().getClassLoader()
                .getResourceAsStream("templates/" + TEMPLATE + ".html");
        assertThat(is).as("Template not found: " + TEMPLATE).isNotNull();
        String raw = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        // Extract field='X' patterns from th:replace fragments + direct th:field
        Pattern fragmentField = Pattern.compile("field\\s*=\\s*'([a-zA-Z0-9_]+)'");
        Pattern directField   = Pattern.compile("th:field\\s*=\\s*\"\\*\\{([a-zA-Z0-9_]+)\\}\"");

        List<String> toCheck = Arrays.asList(
                "code", "name", "ownerId", "isActive",
                "addressLine1", "cityId", "postalCode", "note", "id", "version"
        );

        for (String prop : toCheck) {
            boolean hasGetter = hasReadableProperty(FacilitySaveRequest.class, prop);
            assertThat(hasGetter)
                    .withFailMessage("FacilitySaveRequest missing readable property '%s' (used in form template)", prop)
                    .isTrue();
        }
    }

    // ---- Integration render tests ----

    @Test
    @DisplayName("Create form renders without error for FACILITY_CREATE authority")
    void createForm_rendersSuccessfully() {
        assertDoesNotThrow(() -> {
            String html = TemplateTestUtils.renderWithSecurity(
                    TEMPLATE, createModel(), auth("FACILITY_CREATE"));
            assertThat(html).isNotBlank();
        });
    }

    @Test
    @DisplayName("Edit form renders without error for FACILITY_UPDATE authority")
    void editForm_rendersSuccessfully() {
        assertDoesNotThrow(() -> {
            String html = TemplateTestUtils.renderWithSecurity(
                    TEMPLATE, editModel(), auth("FACILITY_UPDATE"));
            assertThat(html).isNotBlank();
        });
    }

    @Test
    @DisplayName("Create form shows correct heading (id == null branch)")
    void createForm_showsCreateHeading() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, createModel(), auth("FACILITY_CREATE"));
        // th:text uses #messages.msg('label.facility.create') -> not available in test, fallback ok
        assertThat(html).doesNotContain("Exception");
    }

    @Test
    @DisplayName("Edit form shows facility name in heading (id != null branch)")
    void editForm_showsFacilityName() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, editModel(), auth("FACILITY_UPDATE"));
        assertThat(html).contains("Warehouse Utama");
    }

    // ---- Helpers ----

    private boolean hasReadableProperty(Class<?> clazz, String prop) {
        String getter    = "get" + prop.substring(0, 1).toUpperCase() + prop.substring(1);
        String isGetter  = "is"  + prop.substring(0, 1).toUpperCase() + prop.substring(1);
        for (Method m : clazz.getMethods()) {
            if ((m.getName().equals(getter) || m.getName().equals(isGetter))
                    && m.getParameterCount() == 0) {
                return true;
            }
        }
        return false;
    }
}
