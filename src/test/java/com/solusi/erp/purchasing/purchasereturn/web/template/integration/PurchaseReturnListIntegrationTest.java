package com.solusi.erp.purchasing.purchasereturn.web.template.integration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PurchaseReturnListIntegrationTest {

    private final String html = TemplateFiles.read("templates/purchasing/purchase-returns/list.html");

    @Test
    void list_containsRightAlignedCompactSearchSortablePaginationAndStableHtmxTarget() {
        assertThat(html).contains("class=\"d-flex gap-2 ms-auto\"");
        assertThat(html).contains("class=\"input-icon\"");
        assertThat(html).contains("form-control form-control-sm");
        assertThat(html).contains("id=\"purchase-return-table-container\"");
        assertThat(html).contains("hx-target=\"#purchase-return-table-container\"");
        assertThat(html).contains("fragments/table :: sortable");
        assertThat(html).contains("fragments/table :: pagination");
    }

    @Test
    void list_exposesExactlyViewRowActionAndUsesAdaptiveThemeClasses() {
        assertThat(html).contains("btn btn-white btn-sm");
        assertThat(html).contains("#{label.view}");
        assertThat(html).contains("item.status.name() == 'REVERSED'");
        assertThat(html).contains("bg-dark-lt");
        assertThat(html).doesNotContain("#{label.edit}");
        assertThat(html).doesNotContain("#{label.delete}");
        assertThemeSafe(html);
    }

    static void assertThemeSafe(String content) {
        assertThat(content).doesNotContain("bg-light");
        assertThat(content).doesNotContain("bg-white");
        assertThat(content).doesNotContain("text-dark");
    }
}
