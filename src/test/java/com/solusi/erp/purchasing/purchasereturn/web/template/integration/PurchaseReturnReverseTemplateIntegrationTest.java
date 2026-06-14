package com.solusi.erp.purchasing.purchasereturn.web.template.integration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PurchaseReturnReverseTemplateIntegrationTest {

    private final String html = TemplateFiles.read("templates/purchasing/purchase-returns/reverse.html");
    private final String js = TemplateFiles.read("static/js/purchasing/purchase-return/reverse.js");

    @Test
    void reverseTemplate_usesAjaxFormDatePickerAndStableIds() {
        assertThat(html).contains("id=\"purchase-return-reverse-form\"");
        assertThat(html).contains("data-ajax-form=\"true\"");
        assertThat(html).contains("data-redirect-on-success");
        assertThat(html).contains("id=\"reversal-date\"");
        assertThat(html).contains("name=\"reversalDate\"");
        assertThat(html).contains("data-picker=\"date\"");
        assertThat(html).contains("id=\"reversal-reason\"");
        assertThat(html).contains("name=\"reversalReason\"");
        assertThat(html).contains("id=\"purchase-return-reverse-lines\"");
    }

    @Test
    void reverseTemplate_initializesTargetContainerLookupsAndLinePayload() {
        assertThat(html).contains("data-pr-reverse-target-container-select=\"true\"");
        assertThat(html).contains("data-lookup-path=|inventory/containers?facilityId=");
        assertThat(html).contains("lines[${stat.index}].originalMovementId");
        assertThat(html).contains("lines[${stat.index}].targetContainerId");
        assertThat(html).contains("window.PurchaseReturnReverseI18n");
        assertThat(html).contains("/js/purchasing/purchase-return/reverse.js");
    }

    @Test
    void reverseJavascript_validatesTargetsBeforeGlobalAjaxHandler() {
        assertThat(js).contains("initLookup(select, select.getAttribute('data-lookup-path'))");
        assertThat(js).contains("form.addEventListener('submit', validateBeforeSubmit, true)");
        assertThat(js).contains("event.stopImmediatePropagation()");
        assertThat(js).contains("purchase-return-reverse-target-count");
    }
}
