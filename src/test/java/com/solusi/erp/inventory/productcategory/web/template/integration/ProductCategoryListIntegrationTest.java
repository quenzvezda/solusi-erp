package com.solusi.erp.inventory.productcategory.web.template.integration;

import com.solusi.erp.inventory.productcategory.domain.model.ProductCategoryType;
import com.solusi.erp.inventory.productcategory.web.dto.ProductCategorySummaryResponse;
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
 * Integration test for ProductCategory list template — Thymeleaf rendering with SpringSecurityDialect.
 *
 * <p>Tests that {@code sec:authorize} visibility rules for PRODUCT-CATEGORY_CREATE,
 * PRODUCT-CATEGORY_UPDATE, and PRODUCT-CATEGORY_DELETE authorities are correctly applied.</p>
 */
@DisplayName("ProductCategory List — Template Integration Test (sec:authorize visibility)")
@Tag("integration-template")
class ProductCategoryListIntegrationTest {

    private static final String TEMPLATE = "inventory/product-categories/list";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token =
                new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private Map<String, Object> modelWithEmptyPage() {
        org.springframework.data.domain.Page<ProductCategorySummaryResponse> emptyPage =
                new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        return Map.of("page", emptyPage, "keyword", "");
    }

    private Map<String, Object> modelWithOneCategory() {
        ProductCategorySummaryResponse dto = new ProductCategorySummaryResponse();
        dto.setId(1L);
        dto.setCode("CAT-001");
        dto.setName("Electronics");
        dto.setType(ProductCategoryType.STOCK);
        dto.setNote("Sample note");

        org.springframework.data.domain.Page<ProductCategorySummaryResponse> page =
                new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
        return Map.of("page", page, "keyword", "");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Baseline render
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Template renders without error for user with PRODUCT-CATEGORY_READ")
    void withProductCategoryRead_templateRendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("PRODUCT-CATEGORY_READ"));

        assertThat(html).isNotBlank();
        assertThat(html).contains("product-category-table");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. sec:authorize — Add New button (PRODUCT-CATEGORY_CREATE)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("sec:authorize — PRODUCT-CATEGORY_CREATE shows Add New button")
    void withProductCategoryCreate_addButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("PRODUCT-CATEGORY_READ", "PRODUCT-CATEGORY_CREATE"));

        assertThat(html).contains("/inventory/product-categories/create");
    }

    @Test
    @DisplayName("sec:authorize — missing PRODUCT-CATEGORY_CREATE hides Add New button")
    void withoutProductCategoryCreate_addButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("PRODUCT-CATEGORY_READ"));

        assertThat(html).doesNotContain("/inventory/product-categories/create");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. sec:authorize — row-level Edit button (PRODUCT-CATEGORY_UPDATE)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("sec:authorize — PRODUCT-CATEGORY_UPDATE shows Edit button for each row")
    void withProductCategoryUpdate_editButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneCategory(), auth("PRODUCT-CATEGORY_READ", "PRODUCT-CATEGORY_UPDATE"));

        assertThat(html).contains("Electronics");
        assertThat(html).contains("/inventory/product-categories/edit");
    }

    @Test
    @DisplayName("sec:authorize — missing PRODUCT-CATEGORY_UPDATE hides Edit button")
    void withoutProductCategoryUpdate_editButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneCategory(), auth("PRODUCT-CATEGORY_READ"));

        assertThat(html).contains("Electronics");
        assertThat(html).doesNotContain("/inventory/product-categories/edit");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. sec:authorize — row-level Delete button (PRODUCT-CATEGORY_DELETE)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("sec:authorize — PRODUCT-CATEGORY_DELETE shows Delete button for each row")
    void withProductCategoryDelete_deleteButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneCategory(), auth("PRODUCT-CATEGORY_READ", "PRODUCT-CATEGORY_DELETE"));

        assertThat(html).contains("Electronics");
        assertThat(html).contains("modal-delete-1");
    }

    @Test
    @DisplayName("sec:authorize — missing PRODUCT-CATEGORY_DELETE hides Delete button")
    void withoutProductCategoryDelete_deleteButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneCategory(), auth("PRODUCT-CATEGORY_READ"));

        assertThat(html).contains("Electronics");
        assertThat(html).doesNotContain("modal-delete-1");
    }
}
