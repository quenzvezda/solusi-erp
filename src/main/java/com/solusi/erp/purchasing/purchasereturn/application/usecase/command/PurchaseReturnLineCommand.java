package com.solusi.erp.purchasing.purchasereturn.application.usecase.command;

import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnReason;

import java.math.BigDecimal;

public record PurchaseReturnLineCommand(
        Long goodsReceiptLineId,
        boolean serialized,
        BigDecimal quantity,
        BigDecimal baseQuantity,
        Long containerId,
        String serialNumbers,
        PurchaseReturnReason reason,
        String note
) {
}
