package com.solusi.erp.purchasing.purchaserequisition.application.usecase.command;

@FunctionalInterface
public interface DeletePurchaseRequisitionUseCase {
    void execute(Long id);
}
