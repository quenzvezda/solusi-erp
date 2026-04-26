package com.solusi.erp.inventory.goodsreceipt.application.usecase.command;

@FunctionalInterface
public interface DeleteGoodsReceiptUseCase {
    void execute(Long id);
}
