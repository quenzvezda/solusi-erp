package com.solusi.erp.inventory.goodsissue.web.template.integration;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class GoodsIssueCancelTemplateIntegrationTest {

    private final String html = readTemplate();
    private final String js = readScript();

    @Test
    void cancelTemplate_containsAjaxDateReasonAndLineContracts() {
        assertThat(html).contains("layout(~{:: .gi-cancel-content}, ~{:: #page-specific-scripts})");
        assertThat(html).contains("id=\"gi-cancel-form\"");
        assertThat(html).contains("data-ajax-form=\"true\"");
        assertThat(html).contains("class=\"alert-container\"");
        assertThat(html).contains("name=\"reversalDate\"");
        assertThat(html).contains("data-picker=\"date\"");
        assertThat(html).contains("name=\"reason\"");
        assertThat(html).contains("id=\"gi-cancel-lines\"");
        assertThat(html).contains("lines[${stat.index}].originalMovementId");
        assertThat(html).contains("lines[${stat.index}].targetContainerId");
    }

    @Test
    void cancelTemplate_containsTargetContainerAutocompleteTrinityAndFacilityFilter() {
        assertThat(html).contains("data-target-container-select");
        assertThat(html).contains("data-lookup-path=|inventory/containers?facilityId=${cancelView.facilityId()}|");
        assertThat(html).contains("th:value=\"${line.historicalContainerId()}\"");
        assertThat(html).contains("th:text=\"${line.historicalContainerName()}\"");
        assertThat(html).contains("data-subtext=${line.historicalContainerCode()}");
        assertThat(html).contains("/js/inventory/goods-issue/goods-issue-cancel.js");
    }

    @Test
    void cancelPageScript_initializesLookupsValidatesInCaptureAndUpdatesSummary() {
        assertThat(js).contains("window.initLookup");
        assertThat(js).contains("addEventListener('submit', validateBeforeSubmit, true)");
        assertThat(js).contains("gi-cancel-line-count");
        assertThat(js).contains("gi-cancel-target-count");
        assertThat(js).contains("data-target-container-select");
    }

    @Test
    void cancelTemplate_avoidsForbiddenThemeClasses() {
        assertThat(html).doesNotContain("bg-light");
        assertThat(html).doesNotContain("bg-white");
        assertThat(html).doesNotContain("text-dark");
    }

    private static String readTemplate() {
        try {
            return Files.readString(Path.of("src/main/resources/templates/inventory/goods-issues/cancel.html"));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String readScript() {
        try {
            return Files.readString(Path.of("src/main/resources/static/js/inventory/goods-issue/goods-issue-cancel.js"));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
