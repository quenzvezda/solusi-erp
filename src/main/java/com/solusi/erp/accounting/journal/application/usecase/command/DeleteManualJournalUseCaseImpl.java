package com.solusi.erp.accounting.journal.application.usecase.command;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.core.exception.DomainException;

public class DeleteManualJournalUseCaseImpl implements DeleteManualJournalUseCase {

    private final JournalEntryRepository repository;

    public DeleteManualJournalUseCaseImpl(JournalEntryRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(Long id) {
        JournalEntry entry = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.journal.not.found"));
        UpdateManualJournalUseCaseImpl.requireManualDraft(entry);
        repository.deleteById(id);
    }
}
