package com.solusi.erp.inventory.goodsreceipt.application.usecase.query;

import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptReferenceType;

@FunctionalInterface
public interface GetGoodsReceiptCreateViewUseCase {
    GoodsReceipt execute(GoodsReceiptReferenceType referenceType, Long referenceId);
}
