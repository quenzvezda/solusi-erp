package com.solusi.erp.accountspayable.debitmemo.application.usecase.command;

import java.math.BigDecimal;

public record DebitMemoPurchaseReturnLineSource(
        Long purchaseReturnLineId,
        Long productId,
        BigDecimal quantity,
        Long uomId,
        BigDecimal dppAmount,
        BigDecimal taxAmount
) {
}

