package com.solusi.erp.accountspayable.debitmemo.application.usecase.query;

import java.math.BigDecimal;

public record DebitMemoLineView(
        Long id,
        Long purchaseReturnLineId,
        Long productId,
        BigDecimal quantity,
        Long uomId,
        BigDecimal dppAmountOriginal,
        BigDecimal taxAmountOriginal,
        BigDecimal dppAmountBase,
        BigDecimal taxAmountBase
) {
}

