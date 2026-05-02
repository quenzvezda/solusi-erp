package com.solusi.erp.accounting.journal.domain.model;

import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JournalEntryTest {

    @Test
    void debitAndCreditHelpers_setAmountsCorrectly() {
        JournalLine debit = JournalLine.debit(101L, new BigDecimal("500.0000"));
        JournalLine credit = JournalLine.credit(201L, new BigDecimal("500.0000"));

        assertThat(debit.accountId()).isEqualTo(101L);
        assertThat(debit.debitAmount()).isEqualByComparingTo("500.0000");
        assertThat(debit.creditAmount()).isEqualByComparingTo("0");

        assertThat(credit.accountId()).isEqualTo(201L);
        assertThat(credit.debitAmount()).isEqualByComparingTo("0");
        assertThat(credit.creditAmount()).isEqualByComparingTo("500.0000");
    }

    @Test
    void createPosted_setsSourceAndEventMetadata() {
        LocalDate journalDate = LocalDate.now();
        JournalEntry entry = JournalEntry.createPosted(
                SchemaEventType.GOODS_RECEIPT,
                "GOODS_RECEIPT",
                6L,
                "GR-0006",
                journalDate,
                "Auto journal",
                List.of(
                        JournalLine.debit(101L, new BigDecimal("500.0000")),
                        JournalLine.credit(201L, new BigDecimal("500.0000"))
                )
        );

        assertThat(entry.getId()).isNull();
        assertThat(entry.getEventType()).isEqualTo(SchemaEventType.GOODS_RECEIPT);
        assertThat(entry.getSourceType()).isEqualTo("GOODS_RECEIPT");
        assertThat(entry.getSourceId()).isEqualTo(6L);
        assertThat(entry.getSourceCode()).isEqualTo("GR-0006");
        assertThat(entry.getJournalDate()).isEqualTo(journalDate);
        assertThat(entry.getDescription()).isEqualTo("Auto journal");
        assertThat(entry.getStatus()).isEqualTo(JournalStatus.POSTED);
        assertThat(entry.getLines()).hasSize(2);
    }

    @Test
    void validateBalanced_acceptsBalancedEntry() {
        JournalEntry entry = JournalEntry.createPosted(
                SchemaEventType.GOODS_RECEIPT,
                "GOODS_RECEIPT",
                6L,
                "GR-0006",
                LocalDate.now(),
                "Auto journal",
                List.of(
                        JournalLine.debit(101L, new BigDecimal("500.0000")),
                        JournalLine.debit(301L, new BigDecimal("50.0000")),
                        JournalLine.credit(201L, new BigDecimal("550.0000"))
                )
        );

        entry.validateBalanced();
    }

    @Test
    void validateBalanced_throwsWhenNotBalanced() {
        JournalEntry entry = JournalEntry.createPosted(
                SchemaEventType.GOODS_RECEIPT,
                "GOODS_RECEIPT",
                6L,
                "GR-0006",
                LocalDate.now(),
                "Auto journal",
                List.of(
                        JournalLine.debit(101L, new BigDecimal("500.0000")),
                        JournalLine.credit(201L, new BigDecimal("540.0000"))
                )
        );

        assertThatThrownBy(entry::validateBalanced)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.journal.unbalanced");
    }
}
