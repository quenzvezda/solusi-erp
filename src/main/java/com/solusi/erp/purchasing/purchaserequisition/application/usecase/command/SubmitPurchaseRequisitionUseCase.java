package com.solusi.erp.purchasing.purchaserequisition.application.usecase.command;

import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;

@FunctionalInterface
public interface SubmitPurchaseRequisitionUseCase {
    PurchaseRequisition execute(Long id, Long approverId);
}
