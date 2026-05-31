package com.solusi.erp.accounting.journal.infrastructure.adapter;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.accounting.journal.infrastructure.persistence.JournalEntryEntity;
import com.solusi.erp.accounting.journal.infrastructure.persistence.JournalEntryJpaRepository;
import com.solusi.erp.accounting.journal.infrastructure.persistence.JournalPersistenceMapper;
import com.solusi.erp.core.exception.DomainException;

import java.util.Optional;

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
        JournalEntryEntity entity;
        if (entry.getId() == null) {
            entity = mapper.toNewEntity(entry);
        } else {
            entity = jpaRepository.findById(entry.getId())
                    .orElseThrow(() -> new DomainException("msg.error.journal.not.found"));
            mapper.applyToEntity(entry, entity);
        }
        JournalEntryEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public boolean existsBySource(String sourceType, Long sourceId) {
        return jpaRepository.existsBySourceTypeAndSourceId(sourceType, sourceId);
    }

    @Override
    public Optional<JournalEntry> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsReversalOf(Long originalJournalId) {
        return jpaRepository.existsByReversalOfId(originalJournalId);
    }

    @Override
    public Optional<JournalEntry> findReversalOf(Long originalJournalId) {
        return jpaRepository.findByReversalOfId(originalJournalId).map(mapper::toDomain);
    }
}
