package com.solusi.erp.inventory.goodsreceipt.application.usecase.query;

@FunctionalInterface
public interface CountGoodsReceiptsByPoUseCase {
    long execute(Long poId);
}
