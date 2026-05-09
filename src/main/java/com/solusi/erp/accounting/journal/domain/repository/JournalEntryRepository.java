package com.solusi.erp.accounting.journal.domain.repository;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;

public interface JournalEntryRepository {
    JournalEntry save(JournalEntry entry);
    boolean existsBySource(String sourceType, Long sourceId);
}
