package com.solusi.erp.accountspayable.debitmemoallocation.domain.port;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;

import java.math.BigDecimal;
import java.util.Optional;

public interface DebitMemoAllocationSourcePort {

    Optional<DebitMemoSnapshot> findDebitMemoSnapshot(Long debitMemoId);

    Optional<VendorBillSnapshot> findVendorBillSnapshot(Long vendorBillId);

    Page<EligibleVendorBill> findEligibleVendorBills(Long debitMemoId, String keyword, Pageable pageable);

    Page<EligibleDebitMemo> findEligibleDebitMemos(Long vendorBillId, String keyword, Pageable pageable);

    record DebitMemoSnapshot(
            Long id,
            String code,
            Long vendorId,
            Long currencyId,
            BigDecimal grossAmountOriginal,
            BigDecimal dppAmountOriginal,
            BigDecimal taxAmountOriginal,
            BigDecimal dppAmountBase,
            BigDecimal taxAmountBase,
            BigDecimal remainingAmountOriginal,
            BigDecimal appliedGrossOriginal,
            BigDecimal appliedDppOriginal,
            BigDecimal appliedTaxOriginal,
            BigDecimal grirReversalBase,
            BigDecimal taxReversalBase
    ) {
    }

    record VendorBillSnapshot(
            Long id,
            String code,
            Long vendorId,
            Long currencyId,
            BigDecimal totalAmount,
            BigDecimal exchangeRate,
            BigDecimal outstandingAmount
    ) {
    }

    record EligibleVendorBill(
            Long id,
            String code,
            BigDecimal totalAmount,
            BigDecimal outstandingAmount,
            BigDecimal exchangeRate
    ) {
    }

    record EligibleDebitMemo(
            Long id,
            String code,
            BigDecimal grossAmountOriginal,
            BigDecimal remainingAmountOriginal
    ) {
    }
}
