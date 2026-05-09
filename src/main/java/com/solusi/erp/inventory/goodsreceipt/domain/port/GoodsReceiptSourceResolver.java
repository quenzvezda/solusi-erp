package com.solusi.erp.inventory.goodsreceipt.domain.port;

import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptReferenceType;

public interface GoodsReceiptSourceResolver {

    GoodsReceiptReferenceType getReferenceType();

    GoodsReceipt resolve(Long referenceId);
}
