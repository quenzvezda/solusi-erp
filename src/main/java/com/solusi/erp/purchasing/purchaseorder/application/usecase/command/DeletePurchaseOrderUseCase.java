package com.solusi.erp.purchasing.purchaseorder.application.usecase.command;

@FunctionalInterface
public interface DeletePurchaseOrderUseCase {
    void execute(Long id);
}
