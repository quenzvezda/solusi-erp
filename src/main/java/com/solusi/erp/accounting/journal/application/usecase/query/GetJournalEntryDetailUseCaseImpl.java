package com.solusi.erp.accounting.journal.application.usecase.query;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.port.JournalEntryQueryPort;

import java.util.Optional;

public class GetJournalEntryDetailUseCaseImpl implements GetJournalEntryDetailUseCase {
    private final JournalEntryQueryPort queryPort;

    public GetJournalEntryDetailUseCaseImpl(JournalEntryQueryPort queryPort) {
        this.queryPort = queryPort;
    }

    @Override
    public Optional<JournalEntryDetailView> execute(Long id) {
        Optional<JournalEntry> entry = queryPort.getJournalEntryDetail(id);
        if (entry.isEmpty()) {
            return Optional.empty();
        }
        Long reversedById = queryPort.findReversalOf(id)
                .map(JournalEntry::getId)
                .orElse(null);
        return Optional.of(new JournalEntryDetailView(entry.get(), reversedById));
    }
}
