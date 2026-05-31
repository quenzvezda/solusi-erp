package com.solusi.erp.accounting.journal.application.usecase.command;

import com.solusi.erp.accounting.coa.domain.port.CoaPostingValidator;
import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalLine;
import com.solusi.erp.accounting.journal.domain.model.JournalStatus;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.currency.domain.port.CurrencyPostingValidator;

import java.util.List;

public class UpdateManualJournalUseCaseImpl implements UpdateManualJournalUseCase {

    private final JournalEntryRepository repository;
    private final ManualJournalCommandValidator validator;

    public UpdateManualJournalUseCaseImpl(JournalEntryRepository repository,
                                          CoaPostingValidator coaPostingValidator,
                                          CurrencyPostingValidator currencyPostingValidator) {
        this.repository = repository;
        this.validator = new ManualJournalCommandValidator(coaPostingValidator, currencyPostingValidator);
    }

    @Override
    public JournalEntry execute(Long id, ManualJournalCommand command) {
        JournalEntry entry = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.journal.not.found"));
        requireManualDraft(entry);
        List<JournalLine> lines = validator.buildLines(command);
        entry.updateDraft(command.postingDate(), command.currencyId(), command.exchangeRate(),
                command.referenceNo(), command.description(), lines);
        return repository.save(entry);
    }

    static void requireManualDraft(JournalEntry entry) {
        if (!entry.isManual()) {
            throw new DomainException("msg.error.journal.manual.required");
        }
        if (entry.getStatus() != JournalStatus.DRAFT) {
            throw new DomainException("msg.error.journal.draft.required");
        }
    }
}
