package com.solusi.erp.inventory.goodsissue.web.template.integration;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class GoodsIssueListIntegrationTest {

    private final String html = readTemplate();

    @Test
    void listTemplate_containsSearchSortablePaginationAndDeleteContract() {
        assertThat(html).contains("class=\"input-icon\"");
        assertThat(html).contains("form-control form-control-sm");
        assertThat(html).contains("fragments/table :: sortable");
        assertThat(html).contains("fragments/table :: pagination");
        assertThat(html).contains("fragments/modals :: delete-confirm");
        assertThat(html).contains("id=\"gi-table-container\"");
    }

    @Test
    void listTemplate_usesGiAuthoritiesAndAdaptiveClasses() {
        assertThat(html).contains("hasAuthority('GOODS-ISSUE_READ')");
        assertThat(html).contains("hasAuthority('GOODS-ISSUE_UPDATE')");
        assertThat(html).contains("hasAuthority('GOODS-ISSUE_DELETE')");
        assertThat(html).doesNotContain("bg-light");
        assertThat(html).doesNotContain("bg-white");
        assertThat(html).doesNotContain("text-dark");
    }

    private static String readTemplate() {
        try {
            return Files.readString(Path.of("src/main/resources/templates/inventory/goods-issues/list.html"));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
