package com.solusi.erp.purchasing.purchaseorder.application.usecase.query;

import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;

import java.util.Optional;

@FunctionalInterface
public interface GetPurchaseOrderEditViewUseCase {
    Optional<PurchaseOrder> execute(Long id);
}
