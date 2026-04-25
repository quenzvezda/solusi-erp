package com.solusi.erp.purchasing.purchaseorder.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;

@FunctionalInterface
public interface FindPurchaseOrdersUseCase {
    Page<PurchaseOrder> execute(String keyword, Pageable pageable);
}
