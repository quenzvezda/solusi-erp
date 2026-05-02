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

    // ── helpers ──────────────────────────────────────────────────────────────

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

    // ── DTO property check ──────────────────────────────────────────────────

    @Test
    @DisplayName("SchemaSaveRequest has all properties referenced in form template")
    void schemaSaveRequest_hasAllFormBindingProperties() {
        List<String> required = Arrays.asList(
                "eventType", "description", "debitAccountId", "creditAccountId", "taxAccountId",
                "debitAccountName", "creditAccountName", "taxAccountName",
                "isActive", "id", "version");
        for (String prop : required) {
            assertThat(hasReadableProperty(SchemaSaveRequest.class, prop))
                    .withFailMessage("SchemaSaveRequest missing readable property '%s'", prop)
                    .isTrue();
        }
    }

    // ── integration render tests ────────────────────────────────────────────

    @Test
    @DisplayName("Create form renders without error")
    void createForm_rendersSuccessfully() {
        assertDoesNotThrow(() -> {
            String html = TemplateTestUtils.renderWithSecurity(
                    TEMPLATE,
                    Map.of("schemaRequest", newRequest(),
                            "eventTypes", SchemaEventType.values()),
                    auth("ACCOUNTING-SCHEMA_CREATE"));
            assertThat(html).isNotBlank();
        });
    }

    @Test
    @DisplayName("Edit form renders without error")
    void editForm_rendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE,
                Map.of("schemaRequest", editRequest(),
                        "eventTypes", SchemaEventType.values()),
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
        assertThat(raw).contains("schemaRequest");
        assertThat(raw).contains("*{eventType}");
    }

    @Test
    @DisplayName("Account selector wiring uses shared modal selector helper and page scripts slot")
    void accountSelectorWiring_usesSharedModalSelectorHelperAndPageScriptsSlot() throws Exception {
        String template = readResource("templates/" + TEMPLATE + ".html");
        String script = readResource("static/js/schema-form.js");

        assertThat(template).contains("layout(~{:: .schema-form-content}, ~{:: #page-specific-scripts})");
        assertThat(template).contains("id=\"page-specific-scripts\"");
        assertThat(template).contains("th:src=\"@{/js/shared/erp-modal-selector.js}\"");
        assertThat(template).contains("th:src=\"@{/js/schema-form.js}\"");
        assertThat(template).contains("modal-schema-account-selector");
        assertThat(template).contains("schema-account-selector-results");
        assertThat(template).contains("id=\"btn-select-tax-account\"");
        assertThat(template).contains("id=\"btn-clear-tax-account\"");

        assertThat(script).contains("window.ERP.ModalSelector.open");
        assertThat(script).contains("window.ERP.ModalSelector.close");
        assertThat(script).contains("input-tax-account-id");
        assertThat(script).contains("input-tax-account-display");
    }

    @Test
    @DisplayName("Account selector fragment uses paged table layout")
    void accountSelectorFragment_usesPagedTableLayout() throws Exception {
        String template = readResource("templates/accounting/schema/fragments/account-selector-modal.html");

        assertThat(template).contains("hx-target=\"#schema-account-selector-results\"");
        assertThat(template).contains("${page.isEmpty()}");
        assertThat(template).contains("th:each=\"row : ${page.content}\"");
        assertThat(template).contains("fragments/table :: pagination(${page})");
        assertThat(template).contains("table table-vcenter card-table");
        assertThat(template).contains("js-schema-account-pick");
    }

    @Test
    @DisplayName("Selected account display inputs use PO-style form-control class")
    void selectedAccountDisplayInputs_usePoStyleFormControlClass() throws Exception {
        String template = readResource("templates/" + TEMPLATE + ".html");
        String debitDisplayInput = findTagById(template, "input", "input-debit-account-display");
        String creditDisplayInput = findTagById(template, "input", "input-credit-account-display");
        String taxDisplayInput = findTagById(template, "input", "input-tax-account-display");

        assertThat(debitDisplayInput).contains("class=\"form-control\"");
        assertThat(debitDisplayInput).doesNotContain("erp-input");
        assertThat(creditDisplayInput).contains("class=\"form-control\"");
        assertThat(creditDisplayInput).doesNotContain("erp-input");
        assertThat(taxDisplayInput).contains("class=\"form-control\"");
        assertThat(taxDisplayInput).doesNotContain("erp-input");
    }

    @Test
    @DisplayName("Hidden account id fields are placed before input-group to preserve left radius")
    void hiddenAccountIdFields_arePlacedBeforeInputGroup() throws Exception {
        String template = readResource("templates/" + TEMPLATE + ".html");

        assertHiddenBeforeInputGroup(
                template,
                "label.schema.debit.account",
                "id=\"input-debit-account-id\""
        );
        assertHiddenBeforeInputGroup(
                template,
                "label.schema.credit.account",
                "id=\"input-credit-account-id\""
        );
        assertHiddenBeforeInputGroup(
                template,
                "label.schema.tax.account",
                "id=\"input-tax-account-id\""
        );
    }

    // ── utility ─────────────────────────────────────────────────────────────

    private String readResource(String path) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        assertThat(is).as("Resource not found: %s", path).isNotNull();

        try (InputStream in = is) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String findTagById(String html, String tag, String id) {
        String marker = "id=\"" + id + "\"";
        int markerIndex = html.indexOf(marker);
        assertThat(markerIndex).as("Tag with id '%s' not found", id).isGreaterThanOrEqualTo(0);

        int start = html.lastIndexOf("<" + tag, markerIndex);
        assertThat(start).as("Start tag <%s> for id '%s' not found", tag, id).isGreaterThanOrEqualTo(0);

        int end = html.indexOf(">", markerIndex);
        assertThat(end).as("Tag end for id '%s' not found", id).isGreaterThan(markerIndex);

        return html.substring(start, end + 1);
    }

    private void assertHiddenBeforeInputGroup(String html, String labelMarker, String hiddenInputMarker) {
        int sectionStart = html.indexOf(labelMarker);
        assertThat(sectionStart).as("Section marker '%s' not found", labelMarker).isGreaterThanOrEqualTo(0);

        int inputGroupIndex = html.indexOf("<div class=\"input-group\">", sectionStart);
        assertThat(inputGroupIndex).as("Input group not found for section '%s'", labelMarker).isGreaterThanOrEqualTo(0);

        int hiddenInputIndex = html.indexOf(hiddenInputMarker, sectionStart);
        assertThat(hiddenInputIndex).as("Hidden input '%s' not found for section '%s'", hiddenInputMarker, labelMarker)
                .isGreaterThanOrEqualTo(0);

        assertThat(hiddenInputIndex)
                .as("Hidden input '%s' should be placed before input-group for '%s'", hiddenInputMarker, labelMarker)
                .isLessThan(inputGroupIndex);
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
