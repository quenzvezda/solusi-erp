package com.solusi.erp.accounting.journal.infrastructure.persistence;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalLine;
import com.solusi.erp.accounting.journal.domain.model.JournalStatus;
import com.solusi.erp.core.domain.model.AuditMetadata;

import java.util.List;

public class JournalPersistenceMapper {

    public JournalEntryEntity toEntity(JournalEntry domain) {
        return toNewEntity(domain);
    }

    public JournalEntryEntity toNewEntity(JournalEntry domain) {
        JournalEntryEntity entity = new JournalEntryEntity();
        applyToEntity(domain, entity);
        return entity;
    }

    public void applyToEntity(JournalEntry domain, JournalEntryEntity entity) {
        entity.setEventType(domain.getEventType());
        entity.setSourceType(domain.getSourceType());
        entity.setSourceId(domain.getSourceId());
        entity.setSourceCode(domain.getSourceCode());
        entity.setCurrencyId(domain.getCurrencyId());
        entity.setExchangeRate(domain.getExchangeRate());
        entity.setReferenceNo(domain.getReferenceNo());
        entity.setReversalOfId(domain.getReversalOfId());
        entity.setPostingDate(domain.getJournalDate());
        entity.setDescription(domain.getDescription());
        entity.setStatus(domain.getStatus().name());

        entity.getLines().clear();
        List<JournalLine> lines = domain.getLines();
        for (int i = 0; i < lines.size(); i++) {
            entity.getLines().add(toLineEntity(entity, lines.get(i), i + 1));
        }
    }

    public JournalEntry toDomain(JournalEntryEntity entity) {
        List<JournalLine> lines = entity.getLines().stream()
                .map(l -> new JournalLine(
                        l.getAccountId(),
                        l.getDebitAmount(),
                        l.getCreditAmount(),
                        l.getOriginalCurrencyId(),
                        l.getExchangeRate(),
                        l.getOriginalDebitAmount(),
                        l.getOriginalCreditAmount(),
                        l.getDescription()
                ))
                .toList();

        AuditMetadata metadata = new AuditMetadata(
                entity.getId(),
                entity.getVersion() != null ? entity.getVersion().longValue() : null,
                entity.getCreatedDate(),
                entity.getCreatedBy(),
                entity.getUpdatedDate(),
                entity.getUpdatedBy()
        );

        return new JournalEntry(
                metadata,
                entity.getEventType(),
                entity.getSourceType(),
                entity.getSourceId(),
                entity.getSourceCode(),
                entity.getCurrencyId(),
                entity.getExchangeRate(),
                entity.getReferenceNo(),
                entity.getReversalOfId(),
                entity.getPostingDate(),
                entity.getDescription(),
                JournalStatus.valueOf(entity.getStatus()),
                lines
        );
    }

    private JournalLineEntity toLineEntity(JournalEntryEntity entry, JournalLine line, int lineNo) {
        JournalLineEntity lineEntity = new JournalLineEntity();
        lineEntity.setJournalEntry(entry);
        lineEntity.setLineNo(lineNo);
        lineEntity.setAccountId(line.accountId());
        lineEntity.setDebitAmount(line.debitAmount());
        lineEntity.setCreditAmount(line.creditAmount());
        lineEntity.setOriginalCurrencyId(line.originalCurrencyId());
        lineEntity.setExchangeRate(line.exchangeRate());
        lineEntity.setOriginalDebitAmount(line.originalDebitAmount());
        lineEntity.setOriginalCreditAmount(line.originalCreditAmount());
        lineEntity.setDescription(line.description());
        return lineEntity;
    }
}
