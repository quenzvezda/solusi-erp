package com.solusi.erp.purchasing.purchaseorder.application.usecase.command;

import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;

@FunctionalInterface
public interface SubmitPurchaseOrderUseCase {
    PurchaseOrder execute(Long id, Long approverId);
}
