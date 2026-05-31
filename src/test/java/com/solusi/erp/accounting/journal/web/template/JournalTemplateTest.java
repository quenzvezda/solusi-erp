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
    void formTemplate_usesAjaxDateNumericAndDynamicLines() throws Exception {
        String template = readTemplate("accounting/journal/journal-entry-form.html");

        assertThat(template).contains("data-ajax-form=\"true\"");
        assertThat(template).contains("data-picker=\"date\"");
        assertThat(template).contains("data-autonumeric=\"decimal\"");
        assertThat(template).contains("journal-lines-table");
        assertThat(template).contains("journal-entry-form.js");
    }

    @Test
    void detailTemplate_hasManualActionsAndReversalModal() throws Exception {
        String template = readTemplate("accounting/journal/journal-entry-detail.html");

        assertThat(template).contains("btn-post-journal");
        assertThat(template).contains("journal-reversal-modal");
        assertThat(template).contains("journal-entry-detail.js");
        assertThat(template).contains("line.description");
    }

    private String readTemplate(String path) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("templates/" + path);
        assertThat(is).as("Template not found: %s", path).isNotNull();
        try (InputStream in = is) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
