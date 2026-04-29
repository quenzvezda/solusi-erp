package com.solusi.erp.inventory.goodsreceipt.domain.port;

import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptReferenceType;

public interface GoodsReceiptReferenceLookupProvider {

    String resolveReferenceCode(GoodsReceiptReferenceType referenceType, Long referenceId);
}
