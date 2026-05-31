package com.solusi.erp.accounting.journal.domain.port;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalEntryFilter;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import java.util.Optional;

public interface JournalEntryQueryPort {
    Page<JournalEntry> findJournalEntries(JournalEntryFilter filter, Pageable pageable);
    Optional<JournalEntry> getJournalEntryDetail(Long id);
    Optional<JournalEntry> findReversalOf(Long originalJournalId);
}
