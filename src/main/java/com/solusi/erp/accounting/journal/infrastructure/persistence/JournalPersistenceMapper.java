package com.solusi.erp.accounting.journal.infrastructure.persistence;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalLine;
import com.solusi.erp.accounting.journal.domain.model.JournalStatus;
import com.solusi.erp.core.domain.model.AuditMetadata;

import java.util.ArrayList;
import java.util.List;

public class JournalPersistenceMapper {

    public JournalEntryEntity toEntity(JournalEntry domain) {
        JournalEntryEntity entity = new JournalEntryEntity();
        entity.setEventType(domain.getEventType());
        entity.setSourceType(domain.getSourceType());
        entity.setSourceId(domain.getSourceId());
        entity.setSourceCode(domain.getSourceCode());
        entity.setPostingDate(domain.getJournalDate());
        entity.setDescription(domain.getDescription());
        entity.setStatus(domain.getStatus().name());

        List<JournalLineEntity> lineEntities = new ArrayList<>();
        List<JournalLine> lines = domain.getLines();
        for (int i = 0; i < lines.size(); i++) {
            JournalLine line = lines.get(i);
            JournalLineEntity lineEntity = new JournalLineEntity();
            lineEntity.setJournalEntry(entity);
            lineEntity.setLineNo(i + 1);
            lineEntity.setAccountId(line.accountId());
            lineEntity.setDebitAmount(line.debitAmount());
            lineEntity.setCreditAmount(line.creditAmount());
            lineEntity.setOriginalCurrencyId(line.originalCurrencyId());
            lineEntity.setExchangeRate(line.exchangeRate());
            lineEntity.setOriginalDebitAmount(line.originalDebitAmount());
            lineEntity.setOriginalCreditAmount(line.originalCreditAmount());
            lineEntities.add(lineEntity);
        }
        entity.setLines(lineEntities);
        return entity;
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
                        l.getOriginalCreditAmount()
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
                null,
                null,
                null,
                null,
                entity.getPostingDate(),
                entity.getDescription(),
                JournalStatus.valueOf(entity.getStatus()),
                lines
        );
    }
}
