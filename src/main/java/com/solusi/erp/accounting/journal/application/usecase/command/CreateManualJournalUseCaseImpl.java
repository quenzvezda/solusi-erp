package com.solusi.erp.accounting.journal.application.usecase.command;

import com.solusi.erp.accounting.coa.domain.port.CoaPostingValidator;
import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalLine;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.master.currency.domain.port.CurrencyPostingValidator;

import java.util.List;

public class CreateManualJournalUseCaseImpl implements CreateManualJournalUseCase {

    private final JournalEntryRepository repository;
    private final ManualJournalCommandValidator validator;

    public CreateManualJournalUseCaseImpl(JournalEntryRepository repository,
                                          CoaPostingValidator coaPostingValidator,
                                          CurrencyPostingValidator currencyPostingValidator) {
        this.repository = repository;
        this.validator = new ManualJournalCommandValidator(coaPostingValidator, currencyPostingValidator);
    }

    @Override
    public JournalEntry execute(ManualJournalCommand command) {
        List<JournalLine> lines = validator.buildLines(command);
        JournalEntry entry = JournalEntry.createDraft(command.postingDate(), command.currencyId(),
                command.exchangeRate(), command.referenceNo(), command.description(), lines);
        return repository.save(entry);
    }
}
