package com.solusi.erp.accountspayable.debitmemo.application.usecase.query;

import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoSettlementStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DebitMemoSummaryView(
        Long id,
        String code,
        LocalDate memoDate,
        Long vendorId,
        Long currencyId,
        Long purchaseReturnId,
        String purchaseReturnCode,
        BigDecimal grossAmountOriginal,
        BigDecimal settledAmount,
        BigDecimal refundedAmount,
        BigDecimal remainingAmount,
        DebitMemoSettlementStatus settlementStatus
) {
}

