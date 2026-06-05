package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command;

import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationLine;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.port.DebitMemoAllocationSourcePort;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.service.DebitMemoAllocationProrationService;
import com.solusi.erp.core.exception.DomainException;

import java.util.List;

class DebitMemoAllocationDraftFactory {

    private final DebitMemoAllocationSourcePort sourcePort;
    private final DebitMemoAllocationProrationService prorationService;

    DebitMemoAllocationDraftFactory(DebitMemoAllocationSourcePort sourcePort,
                                    DebitMemoAllocationProrationService prorationService) {
        this.sourcePort = sourcePort;
        this.prorationService = prorationService;
    }

    DraftLines buildLines(DebitMemoAllocationSourcePort.DebitMemoSnapshot debitMemo,
                          List<DebitMemoAllocationLineCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            throw new DomainException("msg.error.debit-memo-allocation.lines-required");
        }
        List<DebitMemoAllocationProrationService.AllocationInput> inputs = commands.stream()
                .map(command -> toInput(debitMemo, command))
                .toList();
        List<DebitMemoAllocationLine> lines = prorationService.prorate(
                new DebitMemoAllocationProrationService.DebitMemoSnapshot(
                        debitMemo.grossAmountOriginal(),
                        debitMemo.dppAmountOriginal(),
                        debitMemo.taxAmountOriginal(),
                        debitMemo.dppAmountBase(),
                        debitMemo.taxAmountBase()
                ),
                new DebitMemoAllocationProrationService.AppliedTotals(
                        debitMemo.appliedGrossOriginal(),
                        debitMemo.appliedDppOriginal(),
                        debitMemo.appliedTaxOriginal(),
                        debitMemo.grirReversalBase(),
                        debitMemo.taxReversalBase()
                ),
                inputs
        );
        return new DraftLines(lines);
    }

    private DebitMemoAllocationProrationService.AllocationInput toInput(
            DebitMemoAllocationSourcePort.DebitMemoSnapshot debitMemo,
            DebitMemoAllocationLineCommand command) {
        DebitMemoAllocationSourcePort.VendorBillSnapshot vendorBill = sourcePort.findVendorBillSnapshot(command.vendorBillId())
                .orElseThrow(() -> new DomainException("msg.error.debit-memo-allocation.vendor-bill-not-found"));
        if (!debitMemo.vendorId().equals(vendorBill.vendorId())) {
            throw new DomainException("msg.error.debit-memo-allocation.vendor-mismatch");
        }
        if (!debitMemo.currencyId().equals(vendorBill.currencyId())) {
            throw new DomainException("msg.error.debit-memo-allocation.currency-mismatch");
        }
        return new DebitMemoAllocationProrationService.AllocationInput(
                vendorBill.id(),
                vendorBill.code(),
                debitMemo.remainingAmountOriginal(),
                vendorBill.outstandingAmount(),
                command.appliedGrossOriginal(),
                vendorBill.exchangeRate()
        );
    }

    record DraftLines(List<DebitMemoAllocationLine> lines) {
    }
}
