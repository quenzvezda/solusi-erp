package com.solusi.erp.accounting.journal.web.mapper;

import com.solusi.erp.accounting.coa.domain.port.CoaLookupProvider;
import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalLine;
import com.solusi.erp.accounting.journal.domain.model.JournalStatus;
import com.solusi.erp.accounting.journal.web.dto.JournalEntryDetailResponse;
import com.solusi.erp.accounting.journal.web.dto.JournalEntryListResponse;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JournalEntryWebMapperTest {

    private final AuditMapperHelper auditMapperHelper = mock(AuditMapperHelper.class);
    private final CoaLookupProvider coaLookupProvider = mock(CoaLookupProvider.class);
    private JournalEntryWebMapperImpl mapper;

    @BeforeEach
    void setUp() {
        mapper = new JournalEntryWebMapperImpl();
        mapper.auditMapperHelper = auditMapperHelper;
        mapper.coaLookupProvider = coaLookupProvider;
    }

    @Test
    void toListResponse_mapsPostingDateFromJournalDate() {
        JournalEntryListResponse response = mapper.toListResponse(sampleEntry());

        assertThat(response.getPostingDate()).isEqualTo(LocalDate.of(2026, 5, 6));
    }

    @Test
    void toDetailResponse_enrichesAccountLabelFromCoaLookup() {
        when(coaLookupProvider.resolve(24L)).thenReturn(new LookupDto(24L, "Inventory", "1101.01"));
        when(coaLookupProvider.resolve(33L)).thenReturn(new LookupDto(33L, "GR/IR", "2101.01"));

        JournalEntryDetailResponse response = mapper.toDetailResponse(sampleEntry());

        assertThat(response.getSourceId()).isEqualTo(1L);
        assertThat(response.getPostingDate()).isEqualTo(LocalDate.of(2026, 5, 6));
        assertThat(response.getLines()).hasSize(2);
        assertThat(response.getLines().get(0).getAccountName()).isEqualTo("Inventory");
        assertThat(response.getLines().get(0).getAccountCode()).isEqualTo("1101.01");
        assertThat(response.getLines().get(1).getAccountName()).isEqualTo("GR/IR");
        assertThat(response.getLines().get(1).getAccountCode()).isEqualTo("2101.01");
    }

    private JournalEntry sampleEntry() {
        return new JournalEntry(
                new AuditMetadata(1L, 1L, null, null, null, null),
                SchemaEventType.GOODS_RECEIPT,
                "GOODS_RECEIPT",
                1L,
                "GR-202605-00001",
                LocalDate.of(2026, 5, 6),
                "Auto journal for goods receipt",
                JournalStatus.POSTED,
                List.of(
                        JournalLine.debit(24L, new BigDecimal("12500000.00")),
                        JournalLine.credit(33L, new BigDecimal("12500000.00"))
                )
        );
    }
}
