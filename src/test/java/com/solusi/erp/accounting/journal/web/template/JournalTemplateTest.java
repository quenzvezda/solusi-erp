package com.solusi.erp.accounting.journal.web.template;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Journal Templates — Static Template Check")
class JournalTemplateTest {

    @Test
    void detailTemplate_usesSourceIdAndResolvedAccountFields() throws Exception {
        String template = readTemplate("accounting/journal/journal-entry-detail.html");

        assertThat(template).contains("journal.sourceId");
        assertThat(template).contains("/inventory/goods-receipts/{id}");
        assertThat(template).contains("/purchasing/purchase-returns/view/{id}");
        assertThat(template).contains("line.accountName");
        assertThat(template).contains("line.accountCode");
        assertThat(template).doesNotContain("formatDecimal(line.debitAmount, 1, 'COMMA', 4, 'POINT')");
        assertThat(template).contains("formatDecimal(line.debitAmount, 1, 'COMMA', 2, 'POINT')");
    }

    @Test
    void listTemplate_usesLocalizedEventKeysAndTwoDecimalTotals() throws Exception {
        String template = readTemplate("accounting/journal/journal-entry-list.html");

        assertThat(template).contains("#{label.all}");
        assertThat(template).contains("/accounting/journal-entries/create");
        assertThat(template).contains("label.schema.event.");
        assertThat(template).contains("page.empty");
        assertThat(template).doesNotContain("formatDecimal(entry.totalDebit, 1, 'COMMA', 4, 'POINT')");
        assertThat(template).contains("formatDecimal(entry.totalDebit, 1, 'COMMA', 2, 'POINT')");
    }

    @Test
    void listTemplate_usesSharedTablePaginationFragment() throws Exception {
        String template = readTemplate("accounting/journal/journal-entry-list.html");

        assertThat(template).contains("fragments/table :: pagination(${page})");
        assertThat(template).doesNotContain("fragments/pagination");
    }

    @Test
    void formTemplate_usesAjaxDateNumericAndDynamicLines() throws Exception {
        String template = readTemplate("accounting/journal/journal-entry-form.html");

        assertThat(template).contains("data-ajax-form=\"true\"");
        assertThat(template).contains("data-picker=\"date\"");
        assertThat(template).contains("class=\"form-control erp-input\"");
        assertThat(template).contains("class=\"form-control erp-input erp-number-decimal text-end\"");
        assertThat(template).contains("erp-number-decimal");
        assertThat(template).contains("data-lookup-path=\"master/currencies\"");
        assertThat(template).contains("data-lookup-path=\"accounting/coa\"");
        assertThat(template).contains("class=\"line-row journal-line-row\"");
        assertThat(template).contains("form-control form-control-sm erp-input-sm erp-number-decimal text-end");
        assertThat(template).contains("form-control form-control-sm erp-input-sm");
        assertThat(template).contains("value=\"0.00\"");
        assertThat(template).contains("style=\"width: 30%;\"");
        assertThat(template).contains("style=\"width: 16%;\"");
        assertThat(template).contains("table-layout: fixed");
        assertThat(template).contains("journal-balance-badge");
        assertThat(template).contains("bg-danger-lt");
        assertThat(template).contains("journal-lines-table");
        assertThat(template).contains("erp-currency-rate-lock.js");
        assertThat(template).contains("journal-entry-form.js");
    }

    @Test
    void listAndDetailTemplates_useReadableLightStatusBadges() throws Exception {
        String listTemplate = readTemplate("accounting/journal/journal-entry-list.html");
        String detailTemplate = readTemplate("accounting/journal/journal-entry-detail.html");

        assertThat(listTemplate).contains("bg-warning-lt");
        assertThat(listTemplate).contains("bg-success-lt");
        assertThat(listTemplate).doesNotContain("class=\"badge bg-success\"");
        assertThat(detailTemplate).contains("bg-warning-lt");
        assertThat(detailTemplate).contains("bg-success-lt");
        assertThat(detailTemplate).doesNotContain("class=\"badge bg-success\"");
    }

    @Test
    void detailTemplate_hasManualActionsAndReversalModal() throws Exception {
        String template = readTemplate("accounting/journal/journal-entry-detail.html");

        assertThat(template).contains("btn-post-journal");
        assertThat(template).contains("journal-reversal-modal");
        assertThat(template).contains("journal-reversal-error");
        assertThat(template).contains("data-bs-target=\"#journal-reversal-modal\"");
        assertThat(template).contains("journal-entry-detail.js");
        assertThat(template).contains("line.description");
    }

    @Test
    void pageScripts_containRequiredJournalClientContracts() throws Exception {
        String formTemplate = readTemplate("accounting/journal/journal-entry-form.html");
        String detailTemplate = readTemplate("accounting/journal/journal-entry-detail.html");
        String formJs = readStatic("js/accounting/journal/journal-entry-form.js");
        String detailJs = readStatic("js/accounting/journal/journal-entry-detail.js");

        assertThat(formTemplate).contains("journal-entry-form.js");
        assertThat(detailTemplate).contains("journal-entry-detail.js");
        assertThat(formJs).contains("ERP.CurrencyRateLock");
        assertThat(formJs).contains("journal:balance");
        assertThat(formJs).contains("bg-success-lt");
        assertThat(formJs).contains("bg-danger-lt");
        assertThat(formJs).contains("\"0.00\"");
        assertThat(detailJs).contains("/reverse");
        assertThat(detailJs).contains("reversal-posting-date");
        assertThat(detailJs).contains("erp_pending_success");
        assertThat(detailJs).contains("\"Accept\": \"application/json\"");
        assertThat(detailJs).contains("\"X-Requested-With\": \"XMLHttpRequest\"");
        assertThat(detailJs).contains("payload.message");
        assertThat(detailJs).contains("showWarning");
        assertThat(detailJs).contains("showReversalError");
        assertThat(detailJs).doesNotContain("throw new Error(\"Request failed\")");
    }

    private String readTemplate(String path) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("templates/" + path);
        assertThat(is).as("Template not found: %s", path).isNotNull();
        try (InputStream in = is) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String readStatic(String path) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("static/" + path);
        assertThat(is).as("Static resource not found: %s", path).isNotNull();
        try (InputStream in = is) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
