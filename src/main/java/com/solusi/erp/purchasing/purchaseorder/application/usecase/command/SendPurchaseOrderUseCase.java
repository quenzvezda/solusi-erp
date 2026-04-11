package com.solusi.erp.purchasing.purchaseorder.application.usecase.command;

import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;

@FunctionalInterface
public interface SendPurchaseOrderUseCase {
    PurchaseOrder execute(Long id);
}
