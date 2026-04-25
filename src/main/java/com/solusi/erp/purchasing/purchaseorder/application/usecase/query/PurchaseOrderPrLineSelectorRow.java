package com.solusi.erp.purchasing.purchaseorder.application.usecase.query;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseOrderPrLineSelectorRow(
        Long prLineId,
        Long prId,
        String prCode,
        Long productId,
        String productName,
        String productSubtext,
        BigDecimal requestedQuantity,
        BigDecimal remainingQuantity,
        Long uomId,
        String uomName,
        String uomSubtext,
        BigDecimal estimatedUnitPrice,
        LocalDate requiredDate,
        String note
) {
}
