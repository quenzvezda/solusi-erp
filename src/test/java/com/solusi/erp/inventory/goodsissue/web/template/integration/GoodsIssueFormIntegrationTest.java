package com.solusi.erp.inventory.goodsissue.web.template.integration;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class GoodsIssueFormIntegrationTest {

    private final String html = readTemplate();
    private final String js = readScript();
    private final String sourceLineSelectorFragment = readSourceLineSelectorFragment();

    @Test
    void formTemplate_containsAjaxDateHiddenSourceAndActionContracts() {
        assertThat(html).contains("layout(~{:: .gi-form-content}, ~{:: #page-specific-scripts})");
        assertThat(html).contains("data-ajax-form=\"true\"");
        assertThat(html).contains("data-redirect-on-success=\"/inventory/goods-issues\"");
        assertThat(html).contains("class=\"alert-container\"");
        assertThat(html).contains("id=\"loading-indicator\"");
        assertThat(html).contains("data-picker=\"date\"");
        assertThat(html).contains("th:field=\"*{referenceType}\"");
        assertThat(html).contains("th:field=\"*{referenceId}\"");
        assertThat(html).contains("th:field=\"*{referenceCode}\"");
        assertThat(html).contains("th:field=\"*{partyId}\"");
        assertThat(html).contains("th:field=\"*{partyType}\"");
        assertThat(html).contains("th:field=\"*{facilityId}\"");
        assertThat(html).contains("th:field=\"*{currencyId}\"");
        assertThat(html).contains("th:field=\"*{exchangeRate}\"");
        assertThat(html).contains("ErpForm.postAction(this)");
        assertThat(html).contains("msg.confirm.gi.complete");
        assertThat(html).contains("msg.confirm.gi.cancel");
    }

    @Test
    void formTemplate_containsLineTableValuationAutocompleteAndRowTemplateContracts() {
        assertThat(html).contains("id=\"table-lines\"");
        assertThat(html).contains("class=\"required\"");
        assertThat(html).contains("fragments/inputs :: table-autocomplete");
        assertThat(html).contains("initialValue=${line.productId}");
        assertThat(html).contains("initialText=${line.productName}");
        assertThat(html).contains("initialSubtext=${line.productCode}");
        assertThat(html).contains("initialValue=${line.gridId}");
        assertThat(html).contains("initialText=${line.gridName}");
        assertThat(html).contains("initialSubtext=${line.gridCode}");
        assertThat(html).contains("initialValue=${line.containerId}");
        assertThat(html).contains("initialText=${line.containerName}");
        assertThat(html).contains("initialSubtext=${line.containerCode}");
        assertThat(html).contains("name=\"lines[INDEX].valuationRefType\"");
        assertThat(html).contains("name=\"lines[INDEX].valuationRefId\"");
        assertThat(html).contains("name=\"lines[INDEX].valuationRefLineId\"");
        assertThat(html).contains("id=\"row-template-source\"");
        assertThat(html).doesNotContain("ts-wrapper");
    }

    @Test
    void formTemplate_containsDrawerModalScriptAndThemeSafetyContracts() {
        assertThat(html).contains("id=\"drawer-non-serial\"");
        assertThat(html).contains("id=\"drawer-serial\"");
        assertThat(html).contains("serial-input-container");
        assertThat(html).contains("modal-gi-source-line-selector");
        assertThat(html).contains("gi-source-line-selector-results");
        assertThat(html).contains("GoodsIssuePageConfig");
        assertThat(html).contains("/js/inventory/goods-issue/goods-issue-form.js");
        assertThat(html).contains("bg-secondary-lt");
        assertThat(html).doesNotContain("bg-light");
        assertThat(html).doesNotContain("bg-white");
        assertThat(html).doesNotContain("text-dark");
        assertThat(html).doesNotContain("type=\"number\"");
        assertThat(html).contains("erp-number-decimal");
    }

    @Test
    void pageScript_containsSourceSelectorNumericModalAndSubmitContracts() {
        assertThat(js).contains("modal-gi-source-line-selector");
        assertThat(js).contains("gi-source-line-selector-results");
        assertThat(js).contains("currentReferenceLineIds");
        assertThat(js).contains("duplicateReferenceLine");
        assertThat(js).contains(".input-valuation-ref-type");
        assertThat(js).contains(".input-valuation-ref-id");
        assertThat(js).contains(".input-valuation-ref-line-id");
        assertThat(js).contains("window.ERP.ModalSelector.open");
        assertThat(js).contains("window.ERP.ModalSelector.close");
        assertThat(js).contains("ErpModal.confirm");
        assertThat(js).contains("ErpNumeric.get");
        assertThat(js).contains("ErpNumeric.set");
        assertThat(js).contains("addEventListener('submit', validateBeforeSubmit, true)");
        assertThat(js).contains("beforeunload");
        assertThat(js).doesNotContain("new bootstrap.Modal");
    }

    @Test
    void sourceLineSelectorFragment_containsHtmxPayloadPaginationAndApplyContracts() {
        assertThat(sourceLineSelectorFragment).contains("id=\"gi-source-line-selector-results\"");
        assertThat(sourceLineSelectorFragment).contains("th:fragment=\"gi-source-line-selector-results\"");
        assertThat(sourceLineSelectorFragment).contains("hx-get=\"/inventory/goods-issues/selectors/source-lines\"");
        assertThat(sourceLineSelectorFragment).contains("hx-target=\"#gi-source-line-selector-results\"");
        assertThat(sourceLineSelectorFragment).contains("name=\"excludeReferenceLineIds\"");
        assertThat(sourceLineSelectorFragment).contains("msg.warning.gi.selector.sourceLine.unsupported");
        assertThat(sourceLineSelectorFragment).contains("data-reference-line-id=${row.referenceLineId}");
        assertThat(sourceLineSelectorFragment).contains("data-product-id=${row.productId}");
        assertThat(sourceLineSelectorFragment).contains("data-product-name=${row.productName}");
        assertThat(sourceLineSelectorFragment).contains("data-product-subtext=${row.productCode}");
        assertThat(sourceLineSelectorFragment).contains("data-uom-id=${row.uomId}");
        assertThat(sourceLineSelectorFragment).contains("data-uom-name=${row.uomName}");
        assertThat(sourceLineSelectorFragment).contains("data-uom-subtext=${row.uomCode}");
        assertThat(sourceLineSelectorFragment).contains("data-facility-id=${row.facilityId}");
        assertThat(sourceLineSelectorFragment).contains("data-grid-id=${row.gridId}");
        assertThat(sourceLineSelectorFragment).contains("data-container-id=${row.containerId}");
        assertThat(sourceLineSelectorFragment).contains("data-valuation-ref-type=${row.valuationRefType}");
        assertThat(sourceLineSelectorFragment).contains("data-valuation-ref-id=${row.valuationRefId}");
        assertThat(sourceLineSelectorFragment).contains("data-valuation-ref-line-id=${row.valuationRefLineId}");
        assertThat(sourceLineSelectorFragment).contains("data-serialized=${row.serialized}");
        assertThat(sourceLineSelectorFragment).contains("data-remaining-quantity=${row.remainingQuantity}");
        assertThat(sourceLineSelectorFragment).contains("fragments/table :: pagination(${page})");
        assertThat(sourceLineSelectorFragment).contains("js-gi-source-line-selector-apply");
    }

    private static String readTemplate() {
        try {
            return Files.readString(Path.of("src/main/resources/templates/inventory/goods-issues/form.html"));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String readScript() {
        try {
            return Files.readString(Path.of("src/main/resources/static/js/inventory/goods-issue/goods-issue-form.js"));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String readSourceLineSelectorFragment() {
        try {
            return Files.readString(Path.of("src/main/resources/templates/inventory/goods-issues/fragments/source-line-selector-modal.html"));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
