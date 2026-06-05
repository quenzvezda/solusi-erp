package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command;

import com.solusi.erp.accounting.journal.application.usecase.command.JournalPostingCommand;
import com.solusi.erp.accounting.journal.application.usecase.command.PostJournalForEventUseCase;
import com.solusi.erp.accounting.journal.domain.model.JournalVariable;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemo;
import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoSettlementStatus;
import com.solusi.erp.accountspayable.debitmemo.domain.repository.DebitMemoRepository;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocation;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationLine;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.port.DebitMemoAllocationSourcePort;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.repository.DebitMemoAllocationRepository;
import com.solusi.erp.accountspayable.vendorpayment.domain.port.VendorBillPaymentUpdatePort;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfirmDebitMemoAllocationUseCaseImpl implements ConfirmDebitMemoAllocationUseCase {

    private final DebitMemoAllocationRepository repository;
    private final DebitMemoRepository debitMemoRepository;
    private final DebitMemoAllocationSourcePort sourcePort;
    private final PostJournalForEventUseCase postJournalForEventUseCase;
    private final VendorBillPaymentUpdatePort vendorBillPaymentUpdatePort;
    private final EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase;

    public ConfirmDebitMemoAllocationUseCaseImpl(DebitMemoAllocationRepository repository,
                                                 DebitMemoRepository debitMemoRepository,
                                                 DebitMemoAllocationSourcePort sourcePort,
                                                 PostJournalForEventUseCase postJournalForEventUseCase,
                                                 VendorBillPaymentUpdatePort vendorBillPaymentUpdatePort,
                                                 EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase) {
        this.repository = repository;
        this.debitMemoRepository = debitMemoRepository;
        this.sourcePort = sourcePort;
        this.postJournalForEventUseCase = postJournalForEventUseCase;
        this.vendorBillPaymentUpdatePort = vendorBillPaymentUpdatePort;
        this.ensureOpenPeriodForDateUseCase = ensureOpenPeriodForDateUseCase;
    }

    @Override
    public void execute(Long id) {
        DebitMemoAllocation allocation = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.debit-memo-allocation.not-found"));
        ensureOpenPeriodForDateUseCase.execute(allocation.getAllocationDate());
        List<Long> vendorBillIds = vendorBillIds(allocation);
        sourcePort.lockDebitMemo(allocation.getDebitMemoId());
        sourcePort.lockVendorBills(vendorBillIds);
        DebitMemoAllocationSourcePort.DebitMemoSnapshot debitMemoSnapshot = revalidateDebitMemo(allocation);
        revalidateVendorBills(allocation, debitMemoSnapshot);
        BigDecimal confirmedAppliedAfter = repository.sumConfirmedAppliedByDebitMemoId(allocation.getDebitMemoId())
                .add(allocation.getTotalAppliedGrossOriginal());

        allocation.confirm(null);
        postJournalForEventUseCase.execute(new JournalPostingCommand(
                SchemaEventType.DEBIT_MEMO_APPLICATION,
                "DEBIT_MEMO_ALLOCATION",
                allocation.getId(),
                allocation.getCode(),
                allocation.getAllocationDate(),
                "Auto journal for debit memo allocation " + allocation.getCode(),
                journalValues(allocation)
        ));
        repository.save(allocation);
        updateDebitMemoSettlement(allocation.getDebitMemoId(), confirmedAppliedAfter);
        vendorBillPaymentUpdatePort.updateSettlementStatus(vendorBillIds);
    }

    private DebitMemoAllocationSourcePort.DebitMemoSnapshot revalidateDebitMemo(DebitMemoAllocation allocation) {
        DebitMemoAllocationSourcePort.DebitMemoSnapshot debitMemo = sourcePort.findDebitMemoSnapshot(allocation.getDebitMemoId())
                .orElseThrow(() -> new DomainException("msg.error.debit-memo.not-found"));
        if (allocation.getTotalAppliedGrossOriginal().compareTo(debitMemo.remainingAmountOriginal()) > 0) {
            throw new DomainException("msg.error.debit-memo.remaining.changed");
        }
        return debitMemo;
    }

    private void revalidateVendorBills(DebitMemoAllocation allocation,
                                       DebitMemoAllocationSourcePort.DebitMemoSnapshot debitMemo) {
        for (DebitMemoAllocationLine line : allocation.getLines()) {
            DebitMemoAllocationSourcePort.VendorBillSnapshot vendorBill = sourcePort.findVendorBillSnapshot(line.getVendorBillId())
                    .orElseThrow(() -> new DomainException("msg.error.debit-memo-allocation.vendor-bill-not-found"));
            if (!debitMemo.vendorId().equals(vendorBill.vendorId())) {
                throw new DomainException("msg.error.debit-memo-allocation.vendor-mismatch");
            }
            if (!debitMemo.currencyId().equals(vendorBill.currencyId())) {
                throw new DomainException("msg.error.debit-memo-allocation.currency-mismatch");
            }
            if (line.getAppliedGrossOriginal().compareTo(vendorBill.outstandingAmount()) > 0) {
                throw new DomainException("msg.error.vendor-bill.outstanding.changed");
            }
        }
    }

    private Map<JournalVariable, BigDecimal> journalValues(DebitMemoAllocation allocation) {
        Map<JournalVariable, BigDecimal> values = new HashMap<>();
        values.put(JournalVariable.DMA_AP_AMT, allocation.getTotalApReductionBase());
        values.put(JournalVariable.DMA_GRIR_CLEARING_AMT, allocation.getTotalGrirReversalBase());
        values.put(JournalVariable.DMA_TAX_AMT, allocation.getTotalTaxReversalBase());
        values.put(JournalVariable.DMA_FX_LOSS_AMT, allocation.getTotalFxLossBase());
        values.put(JournalVariable.DMA_FX_GAIN_AMT, allocation.getTotalFxGainBase());
        return values;
    }

    private void updateDebitMemoSettlement(Long debitMemoId, BigDecimal confirmedAppliedAfter) {
        DebitMemo debitMemo = debitMemoRepository.findById(debitMemoId)
                .orElseThrow(() -> new DomainException("msg.error.debit-memo.not-found"));
        if (confirmedAppliedAfter.compareTo(debitMemo.getGrossAmountOriginal()) >= 0) {
            debitMemo.markSettled();
        } else if (debitMemo.getSettlementStatus() == DebitMemoSettlementStatus.OPEN) {
            debitMemo.markPartiallySettled();
        }
        debitMemoRepository.save(debitMemo);
    }

    private List<Long> vendorBillIds(DebitMemoAllocation allocation) {
        return allocation.getLines().stream()
                .map(DebitMemoAllocationLine::getVendorBillId)
                .distinct()
                .toList();
    }
}
