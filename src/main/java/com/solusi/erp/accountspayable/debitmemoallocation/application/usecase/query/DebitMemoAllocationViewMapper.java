package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query;

import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocation;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationLine;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.repository.DebitMemoAllocationHistory;

final class DebitMemoAllocationViewMapper {

    private DebitMemoAllocationViewMapper() {
    }

    static DebitMemoAllocationSummaryView toSummary(DebitMemoAllocation allocation) {
        return new DebitMemoAllocationSummaryView(
                allocation.getId(),
                allocation.getCode(),
                allocation.getDebitMemoId(),
                allocation.getDebitMemoCode(),
                allocation.getAllocationDate(),
                allocation.getStatus(),
                allocation.getTotalAppliedGrossOriginal(),
                allocation.getTotalApReductionBase(),
                allocation.getTotalFxLossBase(),
                allocation.getTotalFxGainBase()
        );
    }

    static DebitMemoAllocationDetailView toDetail(DebitMemoAllocation allocation) {
        return new DebitMemoAllocationDetailView(
                allocation.getId(),
                allocation.getCode(),
                allocation.getDebitMemoId(),
                allocation.getDebitMemoCode(),
                allocation.getAllocationDate(),
                allocation.getStatus(),
                allocation.getTotalAppliedGrossOriginal(),
                allocation.getTotalDppOriginal(),
                allocation.getTotalTaxOriginal(),
                allocation.getTotalGrirReversalBase(),
                allocation.getTotalTaxReversalBase(),
                allocation.getTotalApReductionBase(),
                allocation.getTotalFxLossBase(),
                allocation.getTotalFxGainBase(),
                allocation.getApplyJournalEntryId(),
                allocation.getReversalJournalEntryId(),
                allocation.getReversalDate(),
                allocation.getReversalReason(),
                allocation.getNotes(),
                allocation.getLines().stream().map(DebitMemoAllocationViewMapper::toLine).toList()
        );
    }

    static DebitMemoAllocationHistoryView toHistory(DebitMemoAllocationHistory history) {
        return new DebitMemoAllocationHistoryView(
                history.id(),
                history.code(),
                history.debitMemoId(),
                history.debitMemoCode(),
                history.vendorBillId(),
                history.vendorBillCode(),
                history.allocationDate(),
                history.status(),
                history.appliedGrossOriginal(),
                history.apReductionBase(),
                history.applyJournalEntryId(),
                history.reversalJournalEntryId(),
                history.reversalDate()
        );
    }

    private static DebitMemoAllocationLineView toLine(DebitMemoAllocationLine line) {
        return new DebitMemoAllocationLineView(
                line.getId(),
                line.getVendorBillId(),
                line.getVendorBillCode(),
                line.getDebitMemoRemainingAtDraft(),
                line.getVendorBillOutstandingAtDraft(),
                line.getAppliedGrossOriginal(),
                line.getAppliedDppOriginal(),
                line.getAppliedTaxOriginal(),
                line.getGrirReversalBase(),
                line.getTaxReversalBase(),
                line.getVendorBillExchangeRate(),
                line.getApReductionBase(),
                line.getFxLossBase(),
                line.getFxGainBase()
        );
    }
}
