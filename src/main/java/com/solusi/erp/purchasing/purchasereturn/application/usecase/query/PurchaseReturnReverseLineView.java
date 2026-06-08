package com.solusi.erp.purchasing.purchasereturn.application.usecase.query;

import java.math.BigDecimal;

public record PurchaseReturnReverseLineView(
        Long originalMovementId,
        Long purchaseReturnLineId,
        Long productId,
        String productName,
        String productCode,
        BigDecimal quantity,
        Long targetContainerId,
        String targetContainerName,
        String targetContainerCode,
        String serialNumber
) {
}
