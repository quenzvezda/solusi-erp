package com.solusi.erp.accountspayable.debitmemoallocation.domain.repository;

import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DebitMemoAllocationHistory(
        Long id,
        String code,
        Long debitMemoId,
        String debitMemoCode,
        Long vendorBillId,
        String vendorBillCode,
        LocalDate allocationDate,
        DebitMemoAllocationStatus status,
        BigDecimal appliedGrossOriginal,
        BigDecimal apReductionBase,
        Long applyJournalEntryId,
        Long reversalJournalEntryId,
        LocalDate reversalDate
) {
}
