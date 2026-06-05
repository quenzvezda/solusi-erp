package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query;

import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DebitMemoAllocationDetailView(
        Long id,
        String code,
        Long debitMemoId,
        String debitMemoCode,
        LocalDate allocationDate,
        DebitMemoAllocationStatus status,
        BigDecimal totalAppliedGrossOriginal,
        BigDecimal totalDppOriginal,
        BigDecimal totalTaxOriginal,
        BigDecimal totalGrirReversalBase,
        BigDecimal totalTaxReversalBase,
        BigDecimal totalApReductionBase,
        BigDecimal totalFxLossBase,
        BigDecimal totalFxGainBase,
        Long applyJournalEntryId,
        Long reversalJournalEntryId,
        LocalDate reversalDate,
        String reversalReason,
        String notes,
        List<DebitMemoAllocationLineView> lines
) {
}
