package com.solusi.erp.inventory.goodsissue.web.template.integration;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class GoodsIssueViewIntegrationTest {

    private final String html = readTemplate();

    @Test
    void viewTemplate_containsHeaderJournalLinesActionsAndAudit() {
        assertThat(html).contains("/accounting/journal-entries/{id}");
        assertThat(html).contains("journalLinks.hasOriginalJournal()");
        assertThat(html).contains("journalLinks.hasReversalJournal()");
        assertThat(html).contains("label.gi.journal.original");
        assertThat(html).contains("label.gi.journal.reversal");
        assertThat(html).contains("id=\"table-lines\"");
        assertThat(html).contains("label.qty.issued");
        assertThat(html).contains("label.base.qty");
        assertThat(html).contains("label.unit.cost");
        assertThat(html).contains("ErpForm.postAction(this)");
        assertThat(html).contains("/cancel");
        assertThat(html).contains("gi.referenceType == 'MANUAL'");
        assertThat(html).contains("fragments/audit-info :: audit-info");
    }

    @Test
    void viewTemplate_usesGiAuthoritiesAndAdaptiveClasses() {
        assertThat(html).contains("hasAuthority('GOODS-ISSUE_UPDATE')");
        assertThat(html).contains("hasAuthority('GOODS-ISSUE_COMPLETE')");
        assertThat(html).contains("hasAuthority('GOODS-ISSUE_CANCEL')");
        assertThat(html).doesNotContain("bg-light");
        assertThat(html).doesNotContain("bg-white");
        assertThat(html).doesNotContain("text-dark");
    }

    @Test
    void viewTemplate_placesCompletedCancelActionInHeader() {
        assertThat(html.indexOf("hasAuthority('GOODS-ISSUE_CANCEL')"))
                .isLessThan(html.indexOf("th:href=\"@{/inventory/goods-issues}\""));
    }

    private static String readTemplate() {
        try {
            return Files.readString(Path.of("src/main/resources/templates/inventory/goods-issues/view.html"));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
