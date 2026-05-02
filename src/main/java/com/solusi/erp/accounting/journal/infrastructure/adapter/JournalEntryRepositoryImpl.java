package com.solusi.erp.accounting.journal.infrastructure.adapter;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.accounting.journal.infrastructure.persistence.JournalEntryEntity;
import com.solusi.erp.accounting.journal.infrastructure.persistence.JournalEntryJpaRepository;
import com.solusi.erp.accounting.journal.infrastructure.persistence.JournalPersistenceMapper;

public class JournalEntryRepositoryImpl implements JournalEntryRepository {

    private final JournalEntryJpaRepository jpaRepository;
    private final JournalPersistenceMapper mapper;

    public JournalEntryRepositoryImpl(JournalEntryJpaRepository jpaRepository,
                                      JournalPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public JournalEntry save(JournalEntry entry) {
        JournalEntryEntity entity = mapper.toEntity(entry);
        JournalEntryEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public boolean existsBySource(String sourceType, Long sourceId) {
        return jpaRepository.existsBySourceTypeAndSourceId(sourceType, sourceId);
    }
}
