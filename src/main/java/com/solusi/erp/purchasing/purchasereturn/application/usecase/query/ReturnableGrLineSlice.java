package com.solusi.erp.purchasing.purchasereturn.application.usecase.query;

import java.math.BigDecimal;

public record ReturnableGrLineSlice(
        String selectionKey,
        Long goodsReceiptId,
        Long goodsReceiptLineId,
        Long productId,
        String productName,
        String productCode,
        boolean serialized,
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
        BigDecimal outstandingQuantity,
        String valuationReferenceType,
        Long valuationReferenceId,
        Long valuationReferenceLineId,
        BigDecimal unitCost,
        BigDecimal inventoryAmount,
        BigDecimal taxReversalAmount,
        BigDecimal clearingAmount
) {
}
