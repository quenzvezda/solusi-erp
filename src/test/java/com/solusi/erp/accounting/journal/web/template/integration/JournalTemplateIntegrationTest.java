package com.solusi.erp.accounting.journal.web.template.integration;

import com.solusi.erp.accounting.journal.domain.model.JournalEntryFilter;
import com.solusi.erp.accounting.journal.web.dto.JournalEntryDetailResponse;
import com.solusi.erp.accounting.journal.web.dto.JournalEntryListResponse;
import com.solusi.erp.accounting.journal.web.dto.JournalLineResponse;
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

@DisplayName("Journal Templates — Template Integration Test")
@Tag("integration-template")
class JournalTemplateIntegrationTest {

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token = new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    @Test
    void listTemplate_rendersSourceCodeDateAndTwoDecimalAmounts() {
        JournalEntryListResponse row = new JournalEntryListResponse();
        row.setId(1L);
        row.setJournalCode("JNL-000001");
        row.setEventType("GOODS_RECEIPT");
        row.setSourceType("GOODS_RECEIPT");
        row.setSourceCode("GR-202605-00001");
        row.setPostingDate(LocalDate.of(2026, 5, 6));
        row.setTotalDebit(new BigDecimal("13875000.00"));
        row.setTotalCredit(new BigDecimal("13875000.00"));
        row.setStatus("POSTED");

        String html = TemplateTestUtils.renderWithSecurity(
                "accounting/journal/journal-entry-list",
                Map.of(
                        "page", new PageImpl<>(List.of(row), PageRequest.of(0, 20), 1),
                        "filter", new JournalEntryFilter(null, null, null, null, null),
                        "eventTypes", List.of("GOODS_RECEIPT", "VENDOR_BILL", "PURCHASE_RETURN", "MANUAL")
                ),
                auth("JOURNAL-ENTRY_READ")
        );

        assertThat(html).contains("label.schema.event.PURCHASE_RETURN");
        assertThat(html).contains("GR-202605-00001");
        assertThat(html).contains("06/05/2026");
        assertThat(html).contains("13,875,000.00");
        assertThat(html).doesNotContain("13,875,000.0000");
    }

    @Test
    void detailTemplate_rendersResolvedAccountNameLinkAndTwoDecimalAmounts() {
        JournalLineResponse line = new JournalLineResponse();
        line.setAccountId(24L);
        line.setAccountCode("1101.01");
        line.setAccountName("Inventory");
        line.setDebitAmount(new BigDecimal("12500000.00"));
        line.setCreditAmount(BigDecimal.ZERO);

        JournalEntryDetailResponse journal = new JournalEntryDetailResponse();
        journal.setId(1L);
        journal.setJournalCode("JNL-000001");
        journal.setEventType("GOODS_RECEIPT");
        journal.setSourceType("GOODS_RECEIPT");
        journal.setSourceId(1L);
        journal.setSourceCode("GR-202605-00001");
        journal.setPostingDate(LocalDate.of(2026, 5, 6));
        journal.setDescription("Auto journal for goods receipt GR-202605-00001");
        journal.setStatus("POSTED");
        journal.setLines(List.of(line));
        journal.setTotalDebit(new BigDecimal("12500000.00"));
        journal.setTotalCredit(BigDecimal.ZERO);

        String html = TemplateTestUtils.renderWithSecurity(
                "accounting/journal/journal-entry-detail",
                Map.of("journal", journal),
                auth("JOURNAL-ENTRY_READ")
        );

        assertThat(html).contains("Inventory");
        assertThat(html).contains("1101.01");
        assertThat(html).contains("06/05/2026");
        assertThat(html).contains("12,500,000.00");
        assertThat(html).doesNotContain("12,500,000.0000");
    }

    @Test
    void detailTemplate_hidesEmptyOptionalMetadataFields() {
        JournalLineResponse line = new JournalLineResponse();
        line.setAccountId(24L);
        line.setAccountCode("1101.01");
        line.setAccountName("Inventory");
        line.setDebitAmount(new BigDecimal("12500000.00"));
        line.setCreditAmount(BigDecimal.ZERO);

        JournalEntryDetailResponse journal = new JournalEntryDetailResponse();
        journal.setId(1L);
        journal.setJournalCode("JNL-000001");
        journal.setEventType("GOODS_RECEIPT");
        journal.setSourceType("GOODS_RECEIPT");
        journal.setSourceId(1L);
        journal.setSourceCode("GR-202605-00001");
        journal.setPostingDate(LocalDate.of(2026, 5, 6));
        journal.setDescription("Auto journal for goods receipt GR-202605-00001");
        journal.setStatus("POSTED");
        journal.setLines(List.of(line));
        journal.setTotalDebit(new BigDecimal("12500000.00"));
        journal.setTotalCredit(BigDecimal.ZERO);

        String html = TemplateTestUtils.renderWithSecurity(
                "accounting/journal/journal-entry-detail",
                Map.of("journal", journal),
                auth("JOURNAL-ENTRY_READ")
        );

        assertThat(html).doesNotContain("label.journal.referenceNo");
        assertThat(html).doesNotContain("label.currency");
        assertThat(html).doesNotContain("label.journal.reversalOf");
        assertThat(html).doesNotContain("label.journal.reversedBy");
        assertThat(html).contains("GR-202605-00001");
        assertThat(html).contains("POSTED");
    }

    @Test
    void detailTemplate_linksPurchaseReturnSourceCode() {
        JournalLineResponse line = new JournalLineResponse();
        line.setAccountId(24L);
        line.setAccountCode("2120");
        line.setAccountName("GR/IR Clearing");
        line.setDebitAmount(new BigDecimal("300.00"));
        line.setCreditAmount(BigDecimal.ZERO);

        JournalEntryDetailResponse journal = new JournalEntryDetailResponse();
        journal.setId(2L);
        journal.setJournalCode("JNL-000002");
        journal.setEventType("PURCHASE_RETURN");
        journal.setSourceType("PURCHASE_RETURN");
        journal.setSourceId(70L);
        journal.setSourceCode("PRTN-0070");
        journal.setPostingDate(LocalDate.of(2026, 6, 1));
        journal.setDescription("Auto journal for purchase return PRTN-0070");
        journal.setStatus("POSTED");
        journal.setLines(List.of(line));
        journal.setTotalDebit(new BigDecimal("300.00"));
        journal.setTotalCredit(BigDecimal.ZERO);

        String html = TemplateTestUtils.renderWithSecurity(
                "accounting/journal/journal-entry-detail",
                Map.of("journal", journal),
                auth("JOURNAL-ENTRY_READ")
        );

        assertThat(html).contains("label.schema.event.PURCHASE_RETURN");
        assertThat(html).contains("PRTN-0070");
        assertThat(html).contains("/purchasing/purchase-returns/view/");
    }
}
