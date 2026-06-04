package com.solusi.erp.accountspayable.debitmemo.application.usecase.query;

import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoSettlementStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DebitMemoDetailView(
        Long id,
        String code,
        Long purchaseReturnId,
        String purchaseReturnCode,
        Long vendorId,
        Long currencyId,
        LocalDate memoDate,
        BigDecimal grossAmountOriginal,
        BigDecimal dppAmountOriginal,
        BigDecimal taxAmountOriginal,
        BigDecimal grossAmountBase,
        BigDecimal dppAmountBase,
        BigDecimal taxAmountBase,
        BigDecimal settledAmount,
        BigDecimal refundedAmount,
        BigDecimal remainingAmount,
        DebitMemoSettlementStatus settlementStatus,
        String supplierMemoNumber,
        LocalDate supplierMemoDate,
        String taxDocumentNumber,
        LocalDate taxDocumentDate,
        String notes,
        List<DebitMemoLineView> lines
) {
}

