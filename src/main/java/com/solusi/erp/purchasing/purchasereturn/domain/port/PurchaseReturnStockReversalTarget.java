package com.solusi.erp.purchasing.purchasereturn.domain.port;

public record PurchaseReturnStockReversalTarget(
        Long originalMovementId,
        Long targetContainerId
) {
}
