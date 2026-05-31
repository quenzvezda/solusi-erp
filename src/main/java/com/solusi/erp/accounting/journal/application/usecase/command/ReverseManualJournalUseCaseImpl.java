package com.solusi.erp.accounting.journal.application.usecase.command;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalStatus;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.core.exception.DomainException;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;

public class ReverseManualJournalUseCaseImpl implements ReverseManualJournalUseCase {

    private final JournalEntryRepository repository;
    private final EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase;

    public ReverseManualJournalUseCaseImpl(JournalEntryRepository repository,
                                           EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase) {
        this.repository = repository;
        this.ensureOpenPeriodForDateUseCase = ensureOpenPeriodForDateUseCase;
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
        if (original.getStatus() != JournalStatus.POSTED) {
            throw new DomainException("msg.error.journal.posted.required");
        }
        if (original.isReversal()) {
            throw new DomainException("msg.error.journal.reversal.chain.not.allowed");
        }
        if (repository.existsReversalOf(id)) {
            throw new DomainException("msg.error.journal.already.reversed");
        }

        ensureOpenPeriodForDateUseCase.execute(reversalDate);
        JournalEntry reversal = original.createReversal(reversalDate, "Reversal of JNL-%06d".formatted(id));
        try {
            return repository.save(reversal);
        } catch (DataIntegrityViolationException ex) {
            throw new DomainException("msg.error.journal.already.reversed");
        }
    }
}
