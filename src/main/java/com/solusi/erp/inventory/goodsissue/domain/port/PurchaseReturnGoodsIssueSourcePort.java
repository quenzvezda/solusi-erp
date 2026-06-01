package com.solusi.erp.inventory.goodsissue.domain.port;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PurchaseReturnGoodsIssueSourcePort {

    Optional<HeaderSnapshot> findHeader(Long purchaseReturnId);

    List<LineSnapshot> findEligibleLines(Long purchaseReturnId);

    boolean hasCompletedGoodsIssue(Long purchaseReturnId);

    record HeaderSnapshot(Long purchaseReturnId,
                          String purchaseReturnCode,
                          Long supplierId,
                          Long facilityId,
                          Long currencyId,
                          BigDecimal exchangeRate,
                          boolean billPosted,
                          Long clearingAccountId) {
    }

    record LineSnapshot(Long purchaseReturnLineId,
                        Long productId,
                        String productCode,
                        String productName,
                        boolean serialized,
                        BigDecimal quantity,
                        BigDecimal baseQuantity,
                        Long uomId,
                        String uomCode,
                        String uomName,
                        Long facilityId,
                        Long gridId,
                        String gridCode,
                        String gridName,
                        Long containerId,
                        String containerCode,
                        String containerName,
                        String serialNumber,
                        Long originalGoodsReceiptId,
                        Long originalGoodsReceiptLineId,
                        String valuationRefType,
                        Long valuationRefId,
                        Long valuationRefLineId,
                        BigDecimal unitCost,
                        BigDecimal inventoryAmount,
                        BigDecimal taxReversalAmount,
                        BigDecimal clearingAmount) {
    }
}
