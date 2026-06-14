package com.solusi.erp.purchasing.purchasereturn.infrastructure.adapter;

import com.solusi.erp.accounting.journal.application.usecase.command.ReversePostedJournalCommand;
import com.solusi.erp.accounting.journal.application.usecase.command.ReversePostedJournalUseCase;
import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnJournalReversalPort;

import java.time.LocalDate;

public class PurchaseReturnJournalReversalAdapter implements PurchaseReturnJournalReversalPort {

    private static final String PURCHASE_RETURN_SOURCE_TYPE = "PURCHASE_RETURN";

    private final JournalEntryRepository journalEntryRepository;
    private final ReversePostedJournalUseCase reversePostedJournalUseCase;

    public PurchaseReturnJournalReversalAdapter(JournalEntryRepository journalEntryRepository,
                                                ReversePostedJournalUseCase reversePostedJournalUseCase) {
        this.journalEntryRepository = journalEntryRepository;
        this.reversePostedJournalUseCase = reversePostedJournalUseCase;
    }

    @Override
    public Long reverseOriginalPurchaseReturnJournal(Long purchaseReturnId,
                                                     LocalDate reversalDate,
                                                     String reversalReason) {
        JournalEntry original = journalEntryRepository.findBySource(PURCHASE_RETURN_SOURCE_TYPE, purchaseReturnId)
                .orElseThrow(() -> new DomainException("msg.error.purchase-return.reverse.journal-not-found"));
        JournalEntry reversal = reversePostedJournalUseCase.execute(new ReversePostedJournalCommand(
                original.getId(),
                reversalDate,
                reversalReason
        ));
        return reversal.getId();
    }
}
