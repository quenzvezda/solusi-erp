package com.solusi.erp.purchasing.purchaserequisition.application.usecase.query;

import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;

import java.util.Optional;

@FunctionalInterface
public interface GetPurchaseRequisitionEditViewUseCase {
    Optional<PurchaseRequisition> execute(Long id);
}
