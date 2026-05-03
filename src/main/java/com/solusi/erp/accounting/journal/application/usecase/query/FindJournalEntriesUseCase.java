package com.solusi.erp.accounting.journal.application.usecase.query;
import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalEntryFilter;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;

public interface FindJournalEntriesUseCase {
    Page<JournalEntry> execute(JournalEntryFilter filter, Pageable pageable);
}