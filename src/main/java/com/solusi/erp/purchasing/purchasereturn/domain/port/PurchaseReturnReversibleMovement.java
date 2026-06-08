package com.solusi.erp.purchasing.purchasereturn.domain.port;

import java.math.BigDecimal;

public record PurchaseReturnReversibleMovement(
        Long originalMovementId,
        Long purchaseReturnLineId,
        Long productId,
        String productName,
        String productCode,
        BigDecimal quantity,
        Long sourceContainerId,
        String sourceContainerName,
        String sourceContainerCode,
        String serialNumber
) {
}
