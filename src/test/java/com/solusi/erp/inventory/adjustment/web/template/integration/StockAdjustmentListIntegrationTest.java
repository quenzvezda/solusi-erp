package com.solusi.erp.inventory.adjustment.web.template.integration;

import com.solusi.erp.inventory.adjustment.web.dto.StockAdjustmentSummaryResponse;
import com.solusi.erp.inventory.adjustment.domain.model.AdjustmentStatus;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StockAdjustment List — Template Integration Test (sec:authorize)")
@Tag("integration-template")
class StockAdjustmentListIntegrationTest {

    private static final String TEMPLATE = "inventory/adjustments/list";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token = new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private Map<String, Object> modelWithEmptyPage() {
        org.springframework.data.domain.Page<StockAdjustmentSummaryResponse> emptyPage =
                new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        return Map.of("page", emptyPage, "search", "");
    }

    private Map<String, Object> modelWithOneItem() {
        StockAdjustmentSummaryResponse dto = new StockAdjustmentSummaryResponse();
        dto.setId(1L);
        dto.setCode("ADJ-2025-0001");
        dto.setTransactionDate(LocalDate.now());
        dto.setStatus(AdjustmentStatus.DRAFT);
        dto.setFacilityName("Main WH");
        dto.setTotalAmountLocal(new BigDecimal("500000"));
        dto.setNote("Test note");

        org.springframework.data.domain.Page<StockAdjustmentSummaryResponse> page =
                new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
        return Map.of("page", page, "search", "");
    }

    @Test
    @DisplayName("Template renders without error")
    void templateRendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("STOCK-ADJUSTMENT_READ"));

        assertThat(html).isNotBlank();
        assertThat(html).containsIgnoringCase("stock");
    }

    @Test
    @DisplayName("STOCK-ADJUSTMENT_CREATE shows Add New button")
    void withCreate_addButtonVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("STOCK-ADJUSTMENT_READ", "STOCK-ADJUSTMENT_CREATE"));

        assertThat(html).contains("/inventory/adjustments/create");
    }

    @Test
    @DisplayName("Without STOCK-ADJUSTMENT_CREATE hides Add New button")
    void withoutCreate_addButtonHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("STOCK-ADJUSTMENT_READ"));

        assertThat(html).doesNotContain("/inventory/adjustments/create");
    }

    @Test
    @DisplayName("STOCK-ADJUSTMENT_UPDATE shows Edit button for DRAFT rows")
    void withUpdate_editButtonVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneItem(), auth("STOCK-ADJUSTMENT_READ", "STOCK-ADJUSTMENT_UPDATE"));

        assertThat(html).contains("ADJ-2025-0001");
        assertThat(html).contains("/inventory/adjustments/edit");
    }

    @Test
    @DisplayName("Without STOCK-ADJUSTMENT_UPDATE Edit button is hidden")
    void withoutUpdate_editButtonHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneItem(), auth("STOCK-ADJUSTMENT_READ"));

        assertThat(html).contains("ADJ-2025-0001");
        assertThat(html).doesNotContain("/inventory/adjustments/edit");
    }
}
