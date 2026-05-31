package com.solusi.erp.accounting.journal.domain.model;

import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.core.domain.model.AuditMetadata;
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
        assertThat(entry.getEventType()).isEqualTo(SchemaEventType.GOODS_RECEIPT.name());
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

    @Test
    void journalLine_rejectsBothDebitAndCreditNonZero() {
        assertThatThrownBy(() -> new JournalLine(101L, new BigDecimal("10.0000"), new BigDecimal("5.0000")))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void journalLine_rejectsBothDebitAndCreditZero() {
        assertThatThrownBy(() -> new JournalLine(101L, BigDecimal.ZERO, BigDecimal.ZERO))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void journalLine_rejectsNegativeAmount() {
        assertThatThrownBy(() -> JournalLine.debit(101L, new BigDecimal("-10.0000")))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void createDraft_setsManualIdentityAndHeader() {
        JournalEntry entry = JournalEntry.createDraft(
                LocalDate.of(2026, 5, 31),
                1L,
                new BigDecimal("16000.000000"),
                "REF-001",
                "Manual accrual",
                balancedManualLines()
        );

        assertThat(entry.getEventType()).isEqualTo("MANUAL");
        assertThat(entry.getSourceType()).isEqualTo("MANUAL");
        assertThat(entry.getSourceId()).isNull();
        assertThat(entry.getSourceCode()).isNull();
        assertThat(entry.getCurrencyId()).isEqualTo(1L);
        assertThat(entry.getExchangeRate()).isEqualByComparingTo("16000.000000");
        assertThat(entry.getReferenceNo()).isEqualTo("REF-001");
        assertThat(entry.getReversalOfId()).isNull();
        assertThat(entry.getStatus()).isEqualTo(JournalStatus.DRAFT);
        assertThat(entry.isManual()).isTrue();
        assertThat(entry.isReversal()).isFalse();
        assertThat(entry.getLines()).extracting(JournalLine::description)
                .containsExactly("Debit memo", null);
    }

    @Test
    void createDraft_rejectsZeroOrOneLine() {
        assertThatThrownBy(() -> JournalEntry.createDraft(
                LocalDate.of(2026, 5, 31),
                1L,
                new BigDecimal("16000.000000"),
                null,
                "Manual",
                List.of()
        )).isInstanceOf(DomainException.class);

        assertThatThrownBy(() -> JournalEntry.createDraft(
                LocalDate.of(2026, 5, 31),
                1L,
                new BigDecimal("16000.000000"),
                null,
                "Manual",
                List.of(JournalLine.manualDebit(101L, new BigDecimal("10.0000"), 1L, BigDecimal.ONE, null))
        )).isInstanceOf(DomainException.class);
    }

    @Test
    void createDraft_rejectsMissingOrNonPositiveExchangeRate() {
        assertThatThrownBy(() -> JournalEntry.createDraft(
                LocalDate.of(2026, 5, 31), 1L, null, null, "Manual", balancedManualLines()
        )).isInstanceOf(DomainException.class);

        assertThatThrownBy(() -> JournalEntry.createDraft(
                LocalDate.of(2026, 5, 31), 1L, BigDecimal.ZERO, null, "Manual", balancedManualLines()
        )).isInstanceOf(DomainException.class);

        assertThatThrownBy(() -> JournalEntry.createDraft(
                LocalDate.of(2026, 5, 31), 1L, new BigDecimal("-1"), null, "Manual", balancedManualLines()
        )).isInstanceOf(DomainException.class);
    }

    @Test
    void createDraft_rejectsMissingCurrency() {
        assertThatThrownBy(() -> JournalEntry.createDraft(
                LocalDate.of(2026, 5, 31),
                null,
                new BigDecimal("16000.000000"),
                null,
                "Manual",
                balancedManualLines()
        )).isInstanceOf(DomainException.class);
    }

    @Test
    void createDraft_checksBalanceUsingOriginalAmounts() {
        List<JournalLine> lines = List.of(
                JournalLine.manualDebit(101L, new BigDecimal("100.0000"), 1L, new BigDecimal("16000.000000"), null),
                JournalLine.manualCredit(201L, new BigDecimal("99.0000"), 1L, new BigDecimal("16000.000000"), null)
        );

        assertThatThrownBy(() -> JournalEntry.createDraft(
                LocalDate.of(2026, 5, 31),
                1L,
                new BigDecimal("16000.000000"),
                null,
                "Manual",
                lines
        )).isInstanceOf(DomainException.class);
    }

    @Test
    void updateDraft_replacesHeaderAndLines() {
        JournalEntry entry = JournalEntry.createDraft(
                LocalDate.of(2026, 5, 31),
                1L,
                new BigDecimal("16000.000000"),
                "REF-001",
                "Before",
                balancedManualLines()
        );

        entry.updateDraft(
                LocalDate.of(2026, 6, 1),
                2L,
                new BigDecimal("15000.000000"),
                "REF-002",
                "After",
                List.of(
                        JournalLine.manualDebit(301L, new BigDecimal("20.0000"), 2L, new BigDecimal("15000.000000"), "New debit"),
                        JournalLine.manualCredit(401L, new BigDecimal("20.0000"), 2L, new BigDecimal("15000.000000"), "New credit")
                )
        );

        assertThat(entry.getJournalDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(entry.getCurrencyId()).isEqualTo(2L);
        assertThat(entry.getExchangeRate()).isEqualByComparingTo("15000.000000");
        assertThat(entry.getReferenceNo()).isEqualTo("REF-002");
        assertThat(entry.getDescription()).isEqualTo("After");
        assertThat(entry.getLines()).extracting(JournalLine::accountId)
                .containsExactly(301L, 401L);
    }

    @Test
    void updatePosted_isRejected() {
        JournalEntry entry = JournalEntry.createDraft(
                LocalDate.of(2026, 5, 31),
                1L,
                new BigDecimal("16000.000000"),
                null,
                "Manual",
                balancedManualLines()
        );
        entry.post();

        assertThatThrownBy(() -> entry.updateDraft(
                LocalDate.of(2026, 6, 1),
                1L,
                new BigDecimal("16000.000000"),
                null,
                "After",
                balancedManualLines()
        )).isInstanceOf(DomainException.class);
    }

    @Test
    void postDraft_succeedsAndSecondPostIsRejected() {
        JournalEntry entry = JournalEntry.createDraft(
                LocalDate.of(2026, 5, 31),
                1L,
                new BigDecimal("16000.000000"),
                null,
                "Manual",
                balancedManualLines()
        );

        entry.post();

        assertThat(entry.getStatus()).isEqualTo(JournalStatus.POSTED);
        assertThatThrownBy(entry::post).isInstanceOf(DomainException.class);
    }

    @Test
    void createReversal_swapsDebitAndCreditAmounts() {
        JournalEntry original = postedManualWithId(10L);

        JournalEntry reversal = original.createReversal(
                LocalDate.of(2026, 6, 1),
                "Reverse manual"
        );

        assertThat(reversal.getStatus()).isEqualTo(JournalStatus.POSTED);
        assertThat(reversal.getReversalOfId()).isEqualTo(10L);
        assertThat(reversal.isReversal()).isTrue();
        assertThat(reversal.getJournalDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(reversal.getDescription()).isEqualTo("Reverse manual");
        assertThat(reversal.getLines()).hasSize(2);

        JournalLine first = reversal.getLines().getFirst();
        JournalLine second = reversal.getLines().get(1);
        assertThat(first.creditAmount()).isEqualByComparingTo("1600000.0000000000");
        assertThat(first.originalCreditAmount()).isEqualByComparingTo("100.0000");
        assertThat(first.description()).isEqualTo("Debit memo");
        assertThat(second.debitAmount()).isEqualByComparingTo("1600000.0000000000");
        assertThat(second.originalDebitAmount()).isEqualByComparingTo("100.0000");
    }

    @Test
    void createReversal_rejectsDraftAutoPostedAndReversalRows() {
        JournalEntry draft = JournalEntry.createDraft(
                LocalDate.of(2026, 5, 31),
                1L,
                new BigDecimal("16000.000000"),
                null,
                "Manual",
                balancedManualLines()
        );
        assertThatThrownBy(() -> draft.createReversal(LocalDate.of(2026, 6, 1), "Reverse"))
                .isInstanceOf(DomainException.class);

        JournalEntry autoPosted = JournalEntry.createPosted(
                SchemaEventType.GOODS_RECEIPT,
                "GOODS_RECEIPT",
                6L,
                "GR-0006",
                LocalDate.of(2026, 5, 31),
                "Auto",
                List.of(
                        JournalLine.debit(101L, new BigDecimal("10.0000")),
                        JournalLine.credit(201L, new BigDecimal("10.0000"))
                )
        );
        assertThatThrownBy(() -> autoPosted.createReversal(LocalDate.of(2026, 6, 1), "Reverse"))
                .isInstanceOf(DomainException.class);

        JournalEntry reversal = postedManualWithId(10L).createReversal(LocalDate.of(2026, 6, 1), "Reverse");
        assertThatThrownBy(() -> reversal.createReversal(LocalDate.of(2026, 6, 2), "Reverse again"))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void journalLine_rejectsNullAccount() {
        assertThatThrownBy(() -> JournalLine.manualDebit(
                null,
                new BigDecimal("10.0000"),
                1L,
                BigDecimal.ONE,
                null
        )).isInstanceOf(DomainException.class);
    }

    @Test
    void journalEntry_rejectsEmptyLinesAtConstructionAndFactory() {
        assertThatThrownBy(() -> new JournalEntry(
                AuditMetadata.empty(),
                SchemaEventType.GOODS_RECEIPT.name(),
                "GOODS_RECEIPT",
                6L,
                "GR-0006",
                null,
                null,
                null,
                null,
                LocalDate.now(),
                "Auto journal",
                JournalStatus.POSTED,
                List.of()
        )).isInstanceOf(DomainException.class);

        assertThatThrownBy(() -> JournalEntry.createPosted(
                SchemaEventType.GOODS_RECEIPT,
                "GOODS_RECEIPT",
                6L,
                "GR-0006",
                LocalDate.now(),
                "Auto journal",
                List.of()
        )).isInstanceOf(DomainException.class);
    }

    private static List<JournalLine> balancedManualLines() {
        return List.of(
                JournalLine.manualDebit(101L, new BigDecimal("100.0000"), 1L, new BigDecimal("16000.000000"), "Debit memo"),
                JournalLine.manualCredit(201L, new BigDecimal("100.0000"), 1L, new BigDecimal("16000.000000"), null)
        );
    }

    private static JournalEntry postedManualWithId(Long id) {
        JournalEntry entry = new JournalEntry(
                new AuditMetadata(id, 1L, null, null, null, null),
                "MANUAL",
                "MANUAL",
                null,
                null,
                1L,
                new BigDecimal("16000.000000"),
                "REF-001",
                null,
                LocalDate.of(2026, 5, 31),
                "Original",
                JournalStatus.DRAFT,
                balancedManualLines()
        );
        entry.post();
        return entry;
    }
}
