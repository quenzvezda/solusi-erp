package com.solusi.erp.inventory.stock.domain.model;

import java.time.LocalDate;

public record StockMovementReversalRequest(
        Long originalMovementId,
        Long targetContainerId,
        LocalDate reversalDate,
        String reason
) {
}
