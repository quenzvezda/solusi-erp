package com.solusi.erp.inventory.product.web.template.integration;

import com.solusi.erp.inventory.product.web.dto.ProductSummaryResponse;
import com.solusi.erp.testutils.TemplateTestUtils;
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
 * Integration test for Product list template — Thymeleaf rendering with SpringSecurityDialect.
 *
 * <p>Tests that {@code sec:authorize} visibility rules for PRODUCT_CREATE,
 * PRODUCT_UPDATE, and PRODUCT_DELETE authorities are correctly applied.</p>
 */
@DisplayName("Product List — Template Integration Test (sec:authorize visibility)")
@Tag("integration-template")
class ProductListIntegrationTest {

    private static final String TEMPLATE = "inventory/products/list";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token =
                new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private Map<String, Object> modelWithEmptyPage() {
        org.springframework.data.domain.Page<ProductSummaryResponse> emptyPage =
                new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        return Map.of("page", emptyPage, "keyword", "");
    }

    private Map<String, Object> modelWithOneProduct() {
        ProductSummaryResponse dto = new ProductSummaryResponse();
        dto.setId(1L);
        dto.setCode("PR-001");
        dto.setName("Widget A");
        dto.setBarcode("1234567890");
        dto.setNote("Sample note");
        dto.setCategoryId(1L);
        dto.setCategoryName("Electronics");
        dto.setCategoryType("STOCK");
        dto.setUomCode("PCS");
        dto.setBrandName("Acme");
        dto.setIsActive(Boolean.TRUE);
        dto.setIsSerialized(Boolean.FALSE);

        org.springframework.data.domain.Page<ProductSummaryResponse> page =
                new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
        return Map.of("page", page, "keyword", "");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Baseline render
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Template renders without error for user with PRODUCT_READ")
    void withProductRead_templateRendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("PRODUCT_READ"));

        assertThat(html).isNotBlank();
        assertThat(html).contains("product-table-container");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. sec:authorize — Add New button (PRODUCT_CREATE)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("sec:authorize — PRODUCT_CREATE shows Add New Product button")
    void withProductCreate_addButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("PRODUCT_READ", "PRODUCT_CREATE"));

        assertThat(html).contains("/inventory/products/create");
    }

    @Test
    @DisplayName("sec:authorize — missing PRODUCT_CREATE hides Add New Product button")
    void withoutProductCreate_addButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("PRODUCT_READ"));

        assertThat(html).doesNotContain("/inventory/products/create");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. sec:authorize — row-level Edit button (PRODUCT_UPDATE)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("sec:authorize — PRODUCT_UPDATE shows Edit button for each row")
    void withProductUpdate_editButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneProduct(), auth("PRODUCT_READ", "PRODUCT_UPDATE"));

        assertThat(html).contains("Widget A");
        assertThat(html).contains("/inventory/products/edit");
    }

    @Test
    @DisplayName("sec:authorize — missing PRODUCT_UPDATE hides Edit button")
    void withoutProductUpdate_editButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneProduct(), auth("PRODUCT_READ"));

        assertThat(html).contains("Widget A");
        assertThat(html).doesNotContain("/inventory/products/edit");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. sec:authorize — row-level Delete button (PRODUCT_DELETE)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("sec:authorize — PRODUCT_DELETE shows Delete button for each row")
    void withProductDelete_deleteButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneProduct(), auth("PRODUCT_READ", "PRODUCT_DELETE"));

        assertThat(html).contains("Widget A");
        assertThat(html).contains("modal-delete-1");
    }

    @Test
    @DisplayName("sec:authorize — missing PRODUCT_DELETE hides Delete button")
    void withoutProductDelete_deleteButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneProduct(), auth("PRODUCT_READ"));

        assertThat(html).contains("Widget A");
        assertThat(html).doesNotContain("modal-delete-1");
    }
}
