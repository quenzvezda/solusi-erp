package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query;

import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DebitMemoAllocationSummaryView(
        Long id,
        String code,
        Long debitMemoId,
        String debitMemoCode,
        Long vendorId,
        Long currencyId,
        LocalDate allocationDate,
        DebitMemoAllocationStatus status,
        BigDecimal totalAppliedGrossOriginal,
        BigDecimal totalApReductionBase,
        BigDecimal totalFxLossBase,
        BigDecimal totalFxGainBase
) {
}
