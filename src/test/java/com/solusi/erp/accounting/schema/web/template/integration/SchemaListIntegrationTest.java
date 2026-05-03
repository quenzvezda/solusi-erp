package com.solusi.erp.accounting.schema.web.template.integration;

import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.accounting.schema.web.dto.SchemaSummaryResponse;
import com.solusi.erp.testutils.TemplateTestUtils;
import com.solusi.erp.testutils.TestDtoFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Schema list template — Thymeleaf rendering with SpringSecurityDialect.
 * Validates sec:authorize show/hide buttons per authority.
 */
@DisplayName("Schema List — Template Integration Test (sec:authorize visibility)")
@Tag("integration-template")
class SchemaListIntegrationTest {

    private static final String TEMPLATE = "accounting/schema/list";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token =
                new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private Map<String, Object> modelWithEmptyPage() {
        var page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        return Map.of("page", page, "keyword", "",
                "eventTypes", SchemaEventType.values());
    }

    private Map<String, Object> modelWithOneSchema() {
        SchemaSummaryResponse dto = TestDtoFactory.sampleSchemaSummaryResponse();
        var page = new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
        return Map.of("page", page, "keyword", "",
                "eventTypes", SchemaEventType.values());
    }

    @Test
    @DisplayName("Template renders without error for user with ACCOUNTING-SCHEMA_READ")
    void withSchemaRead_templateRendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("ACCOUNTING-SCHEMA_READ"));
        assertThat(html).isNotBlank();
        assertThat(html).contains("schema-table-container");
    }

    @Test
    @DisplayName("sec:authorize — ACCOUNTING-SCHEMA_CREATE shows Add button")
    void withSchemaCreate_addButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(),
                auth("ACCOUNTING-SCHEMA_READ", "ACCOUNTING-SCHEMA_CREATE"));
        assertThat(html).contains("/accounting/schemas/create");
    }

    @Test
    @DisplayName("sec:authorize — missing ACCOUNTING-SCHEMA_CREATE hides Add button")
    void withoutSchemaCreate_addButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("ACCOUNTING-SCHEMA_READ"));
        assertThat(html).doesNotContain("/accounting/schemas/create");
    }

    @Test
    @DisplayName("sec:authorize — ACCOUNTING-SCHEMA_UPDATE shows Edit button")
    void withSchemaUpdate_editButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneSchema(),
                auth("ACCOUNTING-SCHEMA_READ", "ACCOUNTING-SCHEMA_UPDATE"));
        assertThat(html).contains("Goods receipt schema");
        assertThat(html).contains("/accounting/schemas/edit");
    }

    @Test
    @DisplayName("sec:authorize — missing ACCOUNTING-SCHEMA_UPDATE hides Edit button")
    void withoutSchemaUpdate_editButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneSchema(), auth("ACCOUNTING-SCHEMA_READ"));
        assertThat(html).contains("Goods receipt schema");
        assertThat(html).doesNotContain("/accounting/schemas/edit");
    }

    @Test
    @DisplayName("sec:authorize — ACCOUNTING-SCHEMA_DELETE shows Delete button")
    void withSchemaDelete_deleteButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneSchema(),
                auth("ACCOUNTING-SCHEMA_READ", "ACCOUNTING-SCHEMA_DELETE"));
        assertThat(html).contains("Goods receipt schema");
        assertThat(html).contains("modal-delete-1");
    }

    @Test
    @DisplayName("sec:authorize — missing ACCOUNTING-SCHEMA_DELETE hides Delete button")
    void withoutSchemaDelete_deleteButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneSchema(), auth("ACCOUNTING-SCHEMA_READ"));
        assertThat(html).contains("Goods receipt schema");
        assertThat(html).doesNotContain("modal-delete-1");
    }
}