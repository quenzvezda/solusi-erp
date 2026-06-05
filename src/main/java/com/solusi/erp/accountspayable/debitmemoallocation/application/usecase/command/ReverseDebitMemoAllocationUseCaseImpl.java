package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command;

import com.solusi.erp.accounting.journal.application.usecase.command.ReversePostedJournalCommand;
import com.solusi.erp.accounting.journal.application.usecase.command.ReversePostedJournalUseCase;
import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemo;
import com.solusi.erp.accountspayable.debitmemo.domain.repository.DebitMemoRepository;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocation;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationLine;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationStatus;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.port.DebitMemoAllocationSourcePort;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.repository.DebitMemoAllocationRepository;
import com.solusi.erp.accountspayable.vendorpayment.domain.port.VendorBillPaymentUpdatePort;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ReverseDebitMemoAllocationUseCaseImpl implements ReverseDebitMemoAllocationUseCase {

    private final DebitMemoAllocationRepository repository;
    private final DebitMemoRepository debitMemoRepository;
    private final DebitMemoAllocationSourcePort sourcePort;
    private final ReversePostedJournalUseCase reversePostedJournalUseCase;
    private final VendorBillPaymentUpdatePort vendorBillPaymentUpdatePort;

    public ReverseDebitMemoAllocationUseCaseImpl(DebitMemoAllocationRepository repository,
                                                 DebitMemoRepository debitMemoRepository,
                                                 DebitMemoAllocationSourcePort sourcePort,
                                                 ReversePostedJournalUseCase reversePostedJournalUseCase,
                                                 VendorBillPaymentUpdatePort vendorBillPaymentUpdatePort) {
        this.repository = repository;
        this.debitMemoRepository = debitMemoRepository;
        this.sourcePort = sourcePort;
        this.reversePostedJournalUseCase = reversePostedJournalUseCase;
        this.vendorBillPaymentUpdatePort = vendorBillPaymentUpdatePort;
    }

    @Override
    public void execute(ReverseDebitMemoAllocationCommand command) {
        if (command == null || command.id() == null) {
            throw new DomainException("msg.error.debit-memo-allocation.not-found");
        }
        if (command.reversalDate() == null) {
            throw new DomainException("msg.error.debit-memo-allocation.reversal-date-required");
        }
        if (isBlank(command.reversalReason())) {
            throw new DomainException("msg.error.debit-memo-allocation.reversal-reason-required");
        }

        DebitMemoAllocation allocation = repository.findById(command.id())
                .orElseThrow(() -> new DomainException("msg.error.debit-memo-allocation.not-found"));
        if (allocation.getStatus() != DebitMemoAllocationStatus.CONFIRMED) {
            throw new DomainException("msg.error.debit-memo-allocation.reverse.only-confirmed");
        }
        if (allocation.getApplyJournalEntryId() == null) {
            throw new DomainException("msg.error.debit-memo-allocation.apply-journal-required");
        }

        List<Long> vendorBillIds = vendorBillIds(allocation);
        sourcePort.lockDebitMemo(allocation.getDebitMemoId());
        sourcePort.lockVendorBills(vendorBillIds);
        BigDecimal confirmedAppliedAfter = repository.sumConfirmedAppliedByDebitMemoId(allocation.getDebitMemoId())
                .subtract(allocation.getTotalAppliedGrossOriginal());
        if (confirmedAppliedAfter.compareTo(BigDecimal.ZERO) < 0) {
            confirmedAppliedAfter = BigDecimal.ZERO;
        }

        JournalEntry reversal = reversePostedJournalUseCase.execute(new ReversePostedJournalCommand(
                allocation.getApplyJournalEntryId(),
                command.reversalDate(),
                command.reversalReason().trim()
        ));
        allocation.reverse(reversal.getId(), command.reversalDate(), command.reversalReason().trim());
        repository.save(allocation);
        updateDebitMemoSettlement(allocation.getDebitMemoId(), confirmedAppliedAfter);
        vendorBillPaymentUpdatePort.updateSettlementStatus(vendorBillIds);
    }

    private void updateDebitMemoSettlement(Long debitMemoId, BigDecimal confirmedAppliedAfter) {
        DebitMemo debitMemo = debitMemoRepository.findById(debitMemoId)
                .orElseThrow(() -> new DomainException("msg.error.debit-memo.not-found"));
        debitMemo.refreshSettlementStatus(confirmedAppliedAfter);
        debitMemoRepository.save(debitMemo);
    }

    private List<Long> vendorBillIds(DebitMemoAllocation allocation) {
        return allocation.getLines().stream()
                .map(DebitMemoAllocationLine::getVendorBillId)
                .distinct()
                .toList();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
