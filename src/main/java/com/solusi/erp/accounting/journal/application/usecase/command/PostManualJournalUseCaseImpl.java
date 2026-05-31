package com.solusi.erp.accounting.journal.application.usecase.command;

import com.solusi.erp.accounting.coa.domain.port.CoaPostingValidator;
import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.currency.domain.port.CurrencyPostingValidator;

public class PostManualJournalUseCaseImpl implements PostManualJournalUseCase {

    private final JournalEntryRepository repository;
    private final ManualJournalCommandValidator validator;
    private final EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase;

    public PostManualJournalUseCaseImpl(JournalEntryRepository repository,
                                        CoaPostingValidator coaPostingValidator,
                                        CurrencyPostingValidator currencyPostingValidator,
                                        EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase) {
        this.repository = repository;
        this.validator = new ManualJournalCommandValidator(coaPostingValidator, currencyPostingValidator);
        this.ensureOpenPeriodForDateUseCase = ensureOpenPeriodForDateUseCase;
    }

    @Override
    public JournalEntry execute(Long id) {
        JournalEntry entry = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.journal.not.found"));
        UpdateManualJournalUseCaseImpl.requireManualDraft(entry);
        validator.validateEntry(entry);
        entry.validateBalanced();
        ensureOpenPeriodForDateUseCase.execute(entry.getJournalDate());
        entry.post();
        return repository.save(entry);
    }
}
