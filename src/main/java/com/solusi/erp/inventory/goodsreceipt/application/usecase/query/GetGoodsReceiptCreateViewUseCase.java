package com.solusi.erp.inventory.goodsreceipt.application.usecase.query;

import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;

@FunctionalInterface
public interface GetGoodsReceiptCreateViewUseCase {
    GoodsReceipt execute(Long poId);
}
