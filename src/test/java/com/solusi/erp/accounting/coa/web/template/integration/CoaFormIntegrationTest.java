package com.solusi.erp.accounting.coa.web.template.integration;

import com.solusi.erp.accounting.coa.domain.model.AccountType;
import com.solusi.erp.accounting.coa.web.dto.CoaSaveRequest;
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
 * Form Template Integration Test for COA.
 * model attribute key: "coaRequest" (th:object="${coaRequest}")
 */
@DisplayName("COA Form — Template Integration Test")
@Tag("integration-template")
class CoaFormIntegrationTest {

    private static final String TEMPLATE = "accounting/coa/form";

    // ── helpers ──────────────────────────────────────────────────────────────

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token =
                new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private CoaSaveRequest newRequest() {
        CoaSaveRequest req = TestDtoFactory.sampleCoaSaveRequest();
        req.setId(null);
        req.setVersion(null);
        return req;
    }

    private CoaSaveRequest editRequest() {
        CoaSaveRequest req = TestDtoFactory.sampleCoaSaveRequest();
        req.setId(1L);
        req.setVersion(1);
        return req;
    }

    // ── DTO property check ──────────────────────────────────────────────────

    @Test
    @DisplayName("CoaSaveRequest has all properties referenced in form template")
    void coaSaveRequest_hasAllFormBindingProperties() {
        List<String> required = Arrays.asList(
                "code", "name", "accountType", "parentId", "level",
                "isHeader", "note", "isActive", "id", "version");
        for (String prop : required) {
            assertThat(hasReadableProperty(CoaSaveRequest.class, prop))
                    .withFailMessage("CoaSaveRequest missing readable property '%s'", prop)
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
                    Map.of("coaRequest", newRequest(),
                            "accountTypes", AccountType.values()),
                    auth("ACCOUNTING-COA_CREATE"));
            assertThat(html).isNotBlank();
        });
    }

    @Test
    @DisplayName("Edit form renders without error")
    void editForm_rendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE,
                Map.of("coaRequest", editRequest(),
                        "accountTypes", AccountType.values()),
                auth("ACCOUNTING-COA_UPDATE"));
        assertThat(html).isNotBlank();
    }

    @Test
    @DisplayName("Template contains expected form structure")
    void template_containsExpectedFormStructure() throws Exception {
        InputStream is = getClass().getClassLoader()
                .getResourceAsStream("templates/" + TEMPLATE + ".html");
        assertThat(is).as("Template not found").isNotNull();
        String raw = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        assertThat(raw).contains("coaRequest");
        assertThat(raw).contains("field='code'");
        assertThat(raw).contains("field='name'");
    }

    @Test
    @DisplayName("Parent selector wiring uses shared modal selector helper and page scripts slot")
    void parentSelectorWiring_usesSharedModalSelectorHelperAndPageScriptsSlot() throws Exception {
        String template = readResource("templates/" + TEMPLATE + ".html");
        String script = readResource("static/js/coa-form.js");

        assertThat(template).contains("layout(~{:: .coa-form-content}, ~{:: #page-specific-scripts})");
        assertThat(template).contains("id=\"page-specific-scripts\"");
        assertThat(template).contains("th:src=\"@{/js/shared/erp-modal-selector.js}\"");
        assertThat(template).contains("th:src=\"@{/js/coa-form.js}\"");

        assertThat(script).contains("window.ERP.ModalSelector.open");
        assertThat(script).contains("window.ERP.ModalSelector.close");
    }

    @Test
    @DisplayName("Parent selector fragment uses paged table layout like PO selector")
    void parentSelectorFragment_usesPagedTableLayoutLikePoSelector() throws Exception {
        String template = readResource("templates/accounting/coa/fragments/coa-selector-modal.html");

        assertThat(template).contains("hx-target=\"#coa-parent-selector-results\"");
        assertThat(template).contains("name=\"accountType\"");
        assertThat(template).contains("${page.isEmpty()}");
        assertThat(template).contains("th:each=\"row : ${page.content}\"");
        assertThat(template).contains("fragments/table :: pagination(${page})");
        assertThat(template).contains("table table-vcenter card-table");
    }

    // ── utility ─────────────────────────────────────────────────────────────

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
