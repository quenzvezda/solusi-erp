package com.solusi.erp.inventory.goodsissue.application.usecase.query;

import java.math.BigDecimal;

public record GoodsIssueCancelLineView(
        Long originalMovementId,
        Long productId,
        String productName,
        String productCode,
        BigDecimal quantityIssued,
        Long uomId,
        String uomCode,
        Long historicalContainerId,
        String historicalContainerName,
        String historicalContainerCode,
        String serialNumber
) {
}
