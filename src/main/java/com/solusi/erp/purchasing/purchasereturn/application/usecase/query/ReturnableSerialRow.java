package com.solusi.erp.purchasing.purchasereturn.application.usecase.query;

import java.math.BigDecimal;

public record ReturnableSerialRow(
        String selectionKey,
        Long goodsReceiptId,
        Long goodsReceiptLineId,
        Long productId,
        String productName,
        String productCode,
        Long uomId,
        String uomName,
        String uomCode,
        Long facilityId,
        String facilityName,
        Long gridId,
        String gridName,
        String gridCode,
        Long containerId,
        String containerName,
        String containerCode,
        String serialNumber,
        String valuationReferenceType,
        Long valuationReferenceId,
        Long valuationReferenceLineId,
        BigDecimal unitCost,
        BigDecimal inventoryAmount,
        BigDecimal taxReversalAmount,
        BigDecimal clearingAmount
) {
}
