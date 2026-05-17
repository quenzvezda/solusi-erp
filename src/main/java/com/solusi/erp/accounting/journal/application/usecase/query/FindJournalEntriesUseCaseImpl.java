package com.solusi.erp.accounting.journal.application.usecase.query;
import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalEntryFilter;
import com.solusi.erp.accounting.journal.domain.port.JournalEntryQueryPort;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;

public class FindJournalEntriesUseCaseImpl implements FindJournalEntriesUseCase {
    private final JournalEntryQueryPort queryPort;
    public FindJournalEntriesUseCaseImpl(JournalEntryQueryPort queryPort) {
        this.queryPort = queryPort;
    }
    @Override
    public Page<JournalEntry> execute(JournalEntryFilter filter, Pageable pageable) {
        return queryPort.findJournalEntries(filter, pageable);
    }
}