package com.solusi.erp.purchasing.purchaserequisition.application.usecase.command;

import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;

@FunctionalInterface
public interface CancelPurchaseRequisitionUseCase {
    PurchaseRequisition execute(Long id);
}
