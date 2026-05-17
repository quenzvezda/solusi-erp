package com.solusi.erp.inventory.goodsreceipt.application.usecase.command;

@FunctionalInterface
public interface CompleteGoodsReceiptUseCase {
    void execute(Long id);
}
