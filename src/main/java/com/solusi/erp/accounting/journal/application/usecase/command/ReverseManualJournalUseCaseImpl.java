package com.solusi.erp.accounting.journal.application.usecase.command;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.core.exception.DomainException;

import java.time.LocalDate;

public class ReverseManualJournalUseCaseImpl implements ReverseManualJournalUseCase {

    private final JournalEntryRepository repository;
    private final ReversePostedJournalUseCase reversePostedJournalUseCase;

    public ReverseManualJournalUseCaseImpl(JournalEntryRepository repository,
                                           EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase) {
        this.repository = repository;
        this.reversePostedJournalUseCase = new ReversePostedJournalUseCaseImpl(repository, ensureOpenPeriodForDateUseCase);
    }

    public ReverseManualJournalUseCaseImpl(JournalEntryRepository repository,
                                           ReversePostedJournalUseCase reversePostedJournalUseCase) {
        this.repository = repository;
        this.reversePostedJournalUseCase = reversePostedJournalUseCase;
    }

    @Override
    public JournalEntry execute(Long id, LocalDate reversalDate) {
        if (reversalDate == null) {
            throw new DomainException("msg.error.journal.reversal.date.required");
        }
        JournalEntry original = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.journal.not.found"));
        if (!original.isManual()) {
            throw new DomainException("msg.error.journal.manual.required");
        }
        return reversePostedJournalUseCase.execute(new ReversePostedJournalCommand(
                id,
                reversalDate,
                "Reversal of JNL-%06d".formatted(id)
        ));
    }
}
