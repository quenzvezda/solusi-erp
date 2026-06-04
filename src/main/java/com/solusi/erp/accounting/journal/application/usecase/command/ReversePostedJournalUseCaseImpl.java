package com.solusi.erp.accounting.journal.application.usecase.command;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalStatus;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.core.exception.DomainException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.StringUtils;

public class ReversePostedJournalUseCaseImpl implements ReversePostedJournalUseCase {

    private final JournalEntryRepository repository;
    private final EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase;

    public ReversePostedJournalUseCaseImpl(JournalEntryRepository repository,
                                           EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase) {
        this.repository = repository;
        this.ensureOpenPeriodForDateUseCase = ensureOpenPeriodForDateUseCase;
    }

    @Override
    public JournalEntry execute(ReversePostedJournalCommand command) {
        if (command == null || command.reversalDate() == null) {
            throw new DomainException("msg.error.journal.reversal.date.required");
        }

        Long originalId = command.originalJournalEntryId();
        JournalEntry original = repository.findById(originalId)
                .orElseThrow(() -> new DomainException("msg.error.journal.not.found"));
        if (original.getStatus() != JournalStatus.POSTED) {
            throw new DomainException("msg.error.journal.posted.required");
        }
        if (original.isReversal()) {
            throw new DomainException("msg.error.journal.reversal.chain.not.allowed");
        }
        if (repository.existsReversalOf(originalId)) {
            throw new DomainException("msg.error.journal.already.reversed");
        }

        ensureOpenPeriodForDateUseCase.execute(command.reversalDate());
        JournalEntry reversal = original.createReversal(command.reversalDate(), resolveDescription(command, originalId));
        try {
            return repository.save(reversal);
        } catch (DataIntegrityViolationException ex) {
            throw new DomainException("msg.error.journal.already.reversed");
        }
    }

    private String resolveDescription(ReversePostedJournalCommand command, Long originalId) {
        if (StringUtils.hasText(command.description())) {
            return command.description();
        }
        return "Reversal of JNL-%06d".formatted(originalId);
    }
}
