package com.solusi.erp.accounting.journal.infrastructure.persistence;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalLine;
import com.solusi.erp.accounting.journal.domain.model.JournalStatus;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.core.domain.model.AuditMetadata;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JournalPersistenceMapperTest {

    private final JournalPersistenceMapper mapper = new JournalPersistenceMapper();

    @Test
    void toDomain_readsLegacyAutoPostedRowWithNullHeaderCurrency() {
        JournalEntryEntity entity = entity(
                1L,
                SchemaEventType.GOODS_RECEIPT.name(),
                "GOODS_RECEIPT",
                6L,
                "GR-0006",
                null,
                null,
                null,
                null,
                JournalStatus.POSTED
        );
        entity.getLines().add(line(entity, 1, 101L, "10.0000", "0.0000", null));
        entity.getLines().add(line(entity, 2, 201L, "0.0000", "10.0000", null));

        JournalEntry result = mapper.toDomain(entity);

        assertThat(result.getEventType()).isEqualTo(SchemaEventType.GOODS_RECEIPT.name());
        assertThat(result.getCurrencyId()).isNull();
        assertThat(result.getExchangeRate()).isNull();
        assertThat(result.getReferenceNo()).isNull();
        assertThat(result.getReversalOfId()).isNull();
    }

    @Test
    void toDomain_readsManualRowAndLineMemo() {
        JournalEntryEntity entity = entity(
                2L,
                "MANUAL",
                "MANUAL",
                null,
                null,
                1L,
                new BigDecimal("16000.000000"),
                "REF-001",
                99L,
                JournalStatus.POSTED
        );
        entity.getLines().add(line(entity, 1, 101L, "1600000.0000", "0.0000", "Debit memo"));
        entity.getLines().add(line(entity, 2, 201L, "0.0000", "1600000.0000", "Credit memo"));

        JournalEntry result = mapper.toDomain(entity);

        assertThat(result.getEventType()).isEqualTo("MANUAL");
        assertThat(result.getSourceType()).isEqualTo("MANUAL");
        assertThat(result.getSourceId()).isNull();
        assertThat(result.getCurrencyId()).isEqualTo(1L);
        assertThat(result.getExchangeRate()).isEqualByComparingTo("16000.000000");
        assertThat(result.getReferenceNo()).isEqualTo("REF-001");
        assertThat(result.getReversalOfId()).isEqualTo(99L);
        assertThat(result.getLines()).extracting(JournalLine::description)
                .containsExactly("Debit memo", "Credit memo");
    }

    @Test
    void toNewEntity_mapsManualHeaderAndLineMemo() {
        JournalEntry domain = manualDraft(null);

        JournalEntryEntity entity = mapper.toNewEntity(domain);

        assertThat(entity.getEventType()).isEqualTo("MANUAL");
        assertThat(entity.getSourceId()).isNull();
        assertThat(entity.getCurrencyId()).isEqualTo(1L);
        assertThat(entity.getExchangeRate()).isEqualByComparingTo("16000.000000");
        assertThat(entity.getReferenceNo()).isEqualTo("REF-001");
        assertThat(entity.getReversalOfId()).isNull();
        assertThat(entity.getLines()).hasSize(2);
        assertThat(entity.getLines().getFirst().getJournalEntry()).isSameAs(entity);
        assertThat(entity.getLines().getFirst().getDescription()).isEqualTo("Debit memo");
    }

    @Test
    void applyToEntity_preservesIdentityAndReplacesLines() {
        JournalEntryEntity existing = entity(
                7L,
                "MANUAL",
                "MANUAL",
                null,
                null,
                1L,
                BigDecimal.ONE,
                "OLD",
                null,
                JournalStatus.DRAFT
        );
        existing.setVersion(3);
        existing.getLines().add(line(existing, 1, 101L, "10.0000", "0.0000", "Old"));

        mapper.applyToEntity(manualDraft(7L), existing);

        assertThat(existing.getId()).isEqualTo(7L);
        assertThat(existing.getVersion()).isEqualTo(3);
        assertThat(existing.getReferenceNo()).isEqualTo("REF-001");
        assertThat(existing.getLines()).hasSize(2);
        assertThat(existing.getLines()).extracting(JournalLineEntity::getLineNo)
                .containsExactly(1, 2);
        assertThat(existing.getLines().getFirst().getJournalEntry()).isSameAs(existing);
        assertThat(existing.getLines().getFirst().getDescription()).isEqualTo("Debit memo");
    }

    private static JournalEntry manualDraft(Long id) {
        return new JournalEntry(
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
                "Manual",
                JournalStatus.DRAFT,
                List.of(
                        JournalLine.manualDebit(101L, new BigDecimal("100.0000"), 1L, new BigDecimal("16000.000000"), "Debit memo"),
                        JournalLine.manualCredit(201L, new BigDecimal("100.0000"), 1L, new BigDecimal("16000.000000"), null)
                )
        );
    }

    private static JournalEntryEntity entity(Long id, String eventType, String sourceType, Long sourceId,
                                             String sourceCode, Long currencyId, BigDecimal exchangeRate,
                                             String referenceNo, Long reversalOfId, JournalStatus status) {
        JournalEntryEntity entity = new JournalEntryEntity();
        entity.setId(id);
        entity.setEventType(eventType);
        entity.setSourceType(sourceType);
        entity.setSourceId(sourceId);
        entity.setSourceCode(sourceCode);
        entity.setCurrencyId(currencyId);
        entity.setExchangeRate(exchangeRate);
        entity.setReferenceNo(referenceNo);
        entity.setReversalOfId(reversalOfId);
        entity.setPostingDate(LocalDate.of(2026, 5, 31));
        entity.setDescription("Journal");
        entity.setStatus(status.name());
        return entity;
    }

    private static JournalLineEntity line(JournalEntryEntity entry, int lineNo, Long accountId,
                                          String debit, String credit, String description) {
        JournalLineEntity line = new JournalLineEntity();
        line.setJournalEntry(entry);
        line.setLineNo(lineNo);
        line.setAccountId(accountId);
        line.setDebitAmount(new BigDecimal(debit));
        line.setCreditAmount(new BigDecimal(credit));
        line.setOriginalCurrencyId(entry.getCurrencyId());
        line.setExchangeRate(entry.getExchangeRate());
        line.setOriginalDebitAmount(line.getDebitAmount().signum() > 0 ? new BigDecimal("100.0000") : BigDecimal.ZERO);
        line.setOriginalCreditAmount(line.getCreditAmount().signum() > 0 ? new BigDecimal("100.0000") : BigDecimal.ZERO);
        line.setDescription(description);
        return line;
    }
}
