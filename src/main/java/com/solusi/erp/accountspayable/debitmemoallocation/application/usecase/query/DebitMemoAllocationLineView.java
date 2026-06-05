package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query;

import java.math.BigDecimal;

public record DebitMemoAllocationLineView(
        Long id,
        Long vendorBillId,
        String vendorBillCode,
        BigDecimal debitMemoRemainingAtDraft,
        BigDecimal vendorBillOutstandingAtDraft,
        BigDecimal appliedGrossOriginal,
        BigDecimal appliedDppOriginal,
        BigDecimal appliedTaxOriginal,
        BigDecimal grirReversalBase,
        BigDecimal taxReversalBase,
        BigDecimal vendorBillExchangeRate,
        BigDecimal apReductionBase,
        BigDecimal fxLossBase,
        BigDecimal fxGainBase
) {
}
