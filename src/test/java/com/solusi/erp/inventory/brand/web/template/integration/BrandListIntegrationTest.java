package com.solusi.erp.inventory.brand.web.template.integration;

import com.solusi.erp.inventory.brand.web.dto.BrandSummaryResponse;
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
 * Integration test for Brand list template — Thymeleaf rendering with SpringSecurityDialect.
 *
 * <p><strong>What is tested:</strong></p>
 * <ul>
 *   <li>Template renders without error for a valid model.</li>
 *   <li>{@code sec:authorize="hasAuthority('BRAND_CREATE')"} — the Add-button div is shown
 *       when the user has {@code BRAND_CREATE} and hidden when they do not.</li>
 *   <li>{@code sec:authorize="hasAuthority('BRAND_UPDATE/DELETE')"} — per-row action buttons
 *       follow the same pattern.</li>
 * </ul>
 *
 * <p><strong>Approach:</strong></p>
 * Uses {@link TemplateTestUtils#renderWithSecurity} which:
 * <ol>
 *   <li>Pre-processes the template (strips {@code th:replace} layout includes, converts
 *       {@code @{...}} link expressions to literals).</li>
 *   <li>Registers {@link org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect}
 *       so that {@code sec:authorize} is evaluated at runtime.</li>
 *   <li>Installs the supplied {@link Authentication} into {@code SecurityContextHolder}.</li>
 * </ol>
 *
 * <p><strong>Spring Boot 4.0 note:</strong> {@code @WebMvcTest} and {@code @MockBean} were
 * removed from {@code spring-boot-test-autoconfigure} in Spring Boot 4.0 (Spring Framework 7).
 * This test deliberately avoids those APIs and uses only stable JUnit 5 + Thymeleaf APIs,
 * making it fast (no Spring context startup) and dependency-free (no DB, no servlet container).</p>
 */
@DisplayName("Brand List — Template Integration Test (sec:authorize visibility)")
@Tag("integration-template")
class BrandListIntegrationTest {

    private static final String TEMPLATE = "inventory/brands/list";

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Build an Authentication with the given authority strings. */
    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token =
                new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    /** Build a minimal model map used by the brand list view. */
    private Map<String, Object> modelWithEmptyPage() {
        org.springframework.data.domain.Page<BrandSummaryResponse> emptyPage =
                new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        return Map.of("page", emptyPage, "keyword", "");
    }

    /** Build a model map with one sample Brand item (triggers th:each row rendering). */
    private Map<String, Object> modelWithOneBrand() {
        BrandSummaryResponse dto = new BrandSummaryResponse();
        dto.setId(1L);
        dto.setCode("BR001");
        dto.setName("Acme Brand");
        dto.setNote("Sample note");

        org.springframework.data.domain.Page<BrandSummaryResponse> page =
                new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
        return Map.of("page", page, "keyword", "");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Baseline render
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Template renders without error for user with BRAND_READ")
    void withBrandRead_templateRendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("BRAND_READ"));

        assertThat(html).isNotBlank();
        // HTMX container fragment must be present in output
        assertThat(html).contains("brand-table-container");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. sec:authorize — Add New button (BRAND_CREATE)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("sec:authorize — BRAND_CREATE shows Add New Brand button")
    void withBrandCreate_addButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("BRAND_READ", "BRAND_CREATE"));

        // The Add button's href literal must be present when BRAND_CREATE is granted
        assertThat(html).contains("/inventory/brands/create");
    }

    @Test
    @DisplayName("sec:authorize — missing BRAND_CREATE hides Add New Brand button")
    void withoutBrandCreate_addButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("BRAND_READ"));

        // The Add button's href must NOT appear when BRAND_CREATE is not granted
        assertThat(html).doesNotContain("/inventory/brands/create");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. sec:authorize — row-level Edit button (BRAND_UPDATE)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("sec:authorize — BRAND_UPDATE shows Edit button for each row")
    void withBrandUpdate_editButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneBrand(), auth("BRAND_READ", "BRAND_UPDATE"));

        assertThat(html).contains("Acme Brand");           // row rendered
        assertThat(html).contains("/inventory/brands/edit"); // edit href present
    }

    @Test
    @DisplayName("sec:authorize — missing BRAND_UPDATE hides Edit button")
    void withoutBrandUpdate_editButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneBrand(), auth("BRAND_READ"));

        assertThat(html).contains("Acme Brand");              // row still rendered
        assertThat(html).doesNotContain("/inventory/brands/edit"); // edit href absent
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. sec:authorize — row-level Delete button (BRAND_DELETE)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("sec:authorize — BRAND_DELETE shows Delete button for each row")
    void withBrandDelete_deleteButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneBrand(), auth("BRAND_READ", "BRAND_DELETE"));

        assertThat(html).contains("Acme Brand");
        // Delete button has a modal target using the item id
        assertThat(html).contains("modal-delete-1");
    }

    @Test
    @DisplayName("sec:authorize — missing BRAND_DELETE hides Delete button")
    void withoutBrandDelete_deleteButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneBrand(), auth("BRAND_READ"));

        assertThat(html).contains("Acme Brand");
        assertThat(html).doesNotContain("modal-delete-1");
    }
}
