package com.solusi.erp.inventory.goodsreceipt.infrastructure.adapter;

import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptReferenceType;
import com.solusi.erp.inventory.goodsreceipt.domain.port.GoodsReceiptReferenceLookupProvider;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;

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
}
