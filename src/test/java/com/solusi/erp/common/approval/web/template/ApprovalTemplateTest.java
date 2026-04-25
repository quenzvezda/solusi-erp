package com.solusi.erp.common.approval.web.template;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Approval Template Tests")
class ApprovalTemplateTest {

    private String loadTemplate(String path) throws Exception {
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("templates/" + path + ".html")) {
            assertThat(is).as("Template not found: %s", path).isNotNull();
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    @DisplayName("pending approval list renders document action from documentUrl instead of NEWS-only branch")
    void listTemplate_usesGenericDocumentUrlButton() throws Exception {
        String template = loadTemplate("common/approval/list");

        assertThat(template).contains("th:if=\"${item.documentUrl != null}\"");
        assertThat(template).contains("th:href=\"@{${item.documentUrl}}\"");
        assertThat(template).doesNotContain("th:if=\"${item.referenceType == 'NEWS'}\"");
    }

    @Test
    @DisplayName("approval list document type badge uses i18n key fallback")
    void listTemplate_usesReferenceTypeI18nFallback() throws Exception {
        String template = loadTemplate("common/approval/list");

        assertThat(template).contains("label.approval.reference-type.");
        assertThat(template).contains("#strings.toLowerCase(item.referenceType)");
    }
}
