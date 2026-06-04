package com.solusi.erp.accounting.schema.web.template.integration;

import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.accounting.schema.web.dto.SchemaSaveRequest;
import com.solusi.erp.testutils.TemplateTestUtils;
import com.solusi.erp.testutils.TestDtoFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Form Template Integration Test for Schema.
 * model attribute key: "schemaRequest" (th:object="${schemaRequest}")
 */
@DisplayName("Schema Form — Template Integration Test")
@Tag("integration-template")
class SchemaFormIntegrationTest {

    private static final String TEMPLATE = "accounting/schema/form";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token =
                new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private SchemaSaveRequest newRequest() {
        SchemaSaveRequest req = TestDtoFactory.sampleSchemaSaveRequest();
        req.setId(null);
        req.setVersion(null);
        return req;
    }

    private SchemaSaveRequest editRequest() {
        SchemaSaveRequest req = TestDtoFactory.sampleSchemaSaveRequest();
        req.setId(1L);
        req.setVersion(1);
        return req;
    }

    @Test
    @DisplayName("SchemaSaveRequest has all properties referenced in form template")
    void schemaSaveRequest_hasAllFormBindingProperties() {
        List<String> required = Arrays.asList(
                "eventType", "description", "lines",
                "isActive", "id", "version");
        for (String prop : required) {
            assertThat(hasReadableProperty(SchemaSaveRequest.class, prop))
                    .withFailMessage("SchemaSaveRequest missing readable property '%s'", prop)
                    .isTrue();
        }
    }

    @Test
    @DisplayName("Create form renders without error")
    void createForm_rendersSuccessfully() {
        assertDoesNotThrow(() -> {
            String html = TemplateTestUtils.renderWithSecurity(
                    TEMPLATE,
                    Map.of("schemaRequest", newRequest(),
                            "eventTypes", SchemaEventType.values(),
                            "journalVariables", List.of(),
                            "journalPositions", List.of()),
                    auth("ACCOUNTING-SCHEMA_CREATE"));
            assertThat(html).isNotBlank();
            assertThat(html).contains("value=\"PURCHASE_RETURN\"");
            assertThat(html).contains("label.schema.event.PURCHASE_RETURN");
        });
    }

    @Test
    @DisplayName("Edit form renders without error")
    void editForm_rendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE,
                Map.of("schemaRequest", editRequest(),
                        "eventTypes", SchemaEventType.values(),
                        "journalVariables", List.of(),
                        "journalPositions", List.of()),
                auth("ACCOUNTING-SCHEMA_UPDATE"));
        assertThat(html).isNotBlank();
    }

    @Test
    @DisplayName("Template contains expected form structure")
    void template_containsExpectedFormStructure() throws Exception {
        InputStream is = getClass().getClassLoader()
                .getResourceAsStream("templates/" + TEMPLATE + ".html");
        assertThat(is).as("Template not found").isNotNull();
        String raw = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        assertThat(raw).contains("schema-form");
        assertThat(raw).contains("*{eventType}");
    }

    private String readResource(String path) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        assertThat(is).as("Resource not found: %s", path).isNotNull();
        try (InputStream in = is) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private boolean hasReadableProperty(Class<?> clazz, String prop) {
        String getter = "get" + prop.substring(0, 1).toUpperCase() + prop.substring(1);
        String isGetter = "is" + prop.substring(0, 1).toUpperCase() + prop.substring(1);
        for (java.lang.reflect.Method m : clazz.getMethods()) {
            if ((m.getName().equals(getter) || m.getName().equals(isGetter))
                    && m.getParameterCount() == 0) return true;
        }
        return false;
    }
}
