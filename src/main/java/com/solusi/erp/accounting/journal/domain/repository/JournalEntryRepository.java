package com.solusi.erp.accounting.journal.domain.repository;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;

import java.util.Optional;

public interface JournalEntryRepository {
    JournalEntry save(JournalEntry entry);
    boolean existsBySource(String sourceType, Long sourceId);
    Optional<JournalEntry> findById(Long id);
    void deleteById(Long id);
    boolean existsReversalOf(Long originalJournalId);
    Optional<JournalEntry> findReversalOf(Long originalJournalId);
}
