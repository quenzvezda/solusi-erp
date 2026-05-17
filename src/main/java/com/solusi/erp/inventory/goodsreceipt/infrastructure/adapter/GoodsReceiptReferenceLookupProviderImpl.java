package com.solusi.erp.inventory.goodsreceipt.infrastructure.adapter;

import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptReferenceType;
import com.solusi.erp.inventory.goodsreceipt.domain.port.GoodsReceiptReferenceLookupProvider;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;

import java.util.LinkedHashMap;
import java.util.Map;

public class GoodsReceiptReferenceLookupProviderImpl implements GoodsReceiptReferenceLookupProvider {

    private final PurchaseOrderRepository purchaseOrderRepository;

    public GoodsReceiptReferenceLookupProviderImpl(PurchaseOrderRepository purchaseOrderRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    @Override
    public String resolveReferenceCode(GoodsReceiptReferenceType referenceType, Long referenceId) {
        if (referenceType == null || referenceId == null) {
            return null;
        }
        if (referenceType == GoodsReceiptReferenceType.PURCHASE_ORDER) {
            return purchaseOrderRepository.findById(referenceId)
                    .map(PurchaseOrder::getCode)
                    .orElse(null);
        }
        return null;
    }

    @Override
    public Map<Long, ReferenceLineSnapshot> resolveReferenceLineSnapshots(GoodsReceiptReferenceType referenceType,
                                                                          Long referenceId) {
        if (referenceType != GoodsReceiptReferenceType.PURCHASE_ORDER || referenceId == null) {
            return Map.of();
        }
        return purchaseOrderRepository.findById(referenceId)
            .map(po -> {
                Map<Long, ReferenceLineSnapshot> snapshots = new LinkedHashMap<>();
                po.getLines().forEach(line -> {
                    if (line.getId() == null) {
                        return;
                    }
                    snapshots.put(
                        line.getId(),
                        new ReferenceLineSnapshot(
                            line.getQuantity(),
                            line.getReceivedQuantity(),
                            line.getOutstandingQuantity(),
                            line.getUnitPrice()
                        )
                    );
                });
                return snapshots;
            })
            .orElseGet(Map::of);
    }
}
