package com.solusi.erp.inventory.adjustment.web.template.integration;

import com.solusi.erp.inventory.adjustment.domain.model.AdjustmentStatus;
import com.solusi.erp.inventory.adjustment.web.dto.StockAdjustmentSaveRequest;
import com.solusi.erp.master.currency.web.dto.CurrencySummaryResponse;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StockAdjustment Form — Template Integration Test")
@Tag("integration-template")
class StockAdjustmentFormIntegrationTest {

    private static final String TEMPLATE = "inventory/adjustments/form";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token = new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private StockAdjustmentSaveRequest draftRequest() {
        StockAdjustmentSaveRequest req = new StockAdjustmentSaveRequest();
        req.setId(1L);
        req.setCode("ADJ-001");
        req.setTransactionDate(LocalDate.now());
        req.setCurrencyId(1L);
        req.setFacilityId(1L);
        req.setFacilityName("Main WH");
        req.setExchangeRate(BigDecimal.ONE);
        req.setStatus(AdjustmentStatus.DRAFT);
        req.setLines(List.of());
        return req;
    }

    private StockAdjustmentSaveRequest completedRequest() {
        StockAdjustmentSaveRequest req = draftRequest();
        req.setStatus(AdjustmentStatus.COMPLETED);
        return req;
    }

    private Map<String, Object> modelForDraft() {
        CurrencySummaryResponse defaultCurrency = new CurrencySummaryResponse();
        defaultCurrency.setAlias("IDR");
        return Map.of(
                "stockAdjustment", draftRequest(),
                "currencies", List.of(),
                "defaultCurrency", defaultCurrency
        );
    }

    private Map<String, Object> modelForCompleted() {
        CurrencySummaryResponse defaultCurrency = new CurrencySummaryResponse();
        defaultCurrency.setAlias("IDR");
        return Map.of(
                "stockAdjustment", completedRequest(),
                "currencies", List.of(),
                "defaultCurrency", defaultCurrency
        );
    }

    @Test
    @DisplayName("Form renders without error for DRAFT")
    void draftForm_rendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelForDraft(), auth("STOCK-ADJUSTMENT_UPDATE"));
        assertThat(html).isNotBlank();
        // The form action contains the adjustment id
        assertThat(html).contains("/inventory/adjustments/edit/1");
    }

    @Test
    @DisplayName("DRAFT form: Add Line button is visible")
    void draftForm_addLineButtonVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelForDraft(), auth("STOCK-ADJUSTMENT_UPDATE"));
        // btn-add-line appears as id attribute in the button element
        assertThat(html).contains("id=\"btn-add-line\"");
    }

    @Test
    @DisplayName("COMPLETED form: Add Line button is hidden (isLocked=true)")
    void completedForm_addLineButtonHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelForCompleted(), auth("STOCK-ADJUSTMENT_UPDATE"));
        // btn-add-line button element should not be rendered
        assertThat(html).doesNotContain("id=\"btn-add-line\"");
    }

    @Test
    @DisplayName("DRAFT + STOCK-ADJUSTMENT_PROCESS: process button visible")
    void draftForm_withProcessAuthority_processButtonVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelForDraft(),
                auth("STOCK-ADJUSTMENT_UPDATE", "STOCK-ADJUSTMENT_PROCESS"));
        // btn-process-inventory button element's id attribute
        assertThat(html).contains("id=\"btn-process-inventory\"");
    }

    @Test
    @DisplayName("DRAFT without STOCK-ADJUSTMENT_PROCESS: process button hidden")
    void draftForm_withoutProcessAuthority_processButtonHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelForDraft(), auth("STOCK-ADJUSTMENT_UPDATE"));
        // When process authority is absent, the button id attribute should not be rendered
        assertThat(html).doesNotContain("id=\"btn-process-inventory\"");
    }
}
