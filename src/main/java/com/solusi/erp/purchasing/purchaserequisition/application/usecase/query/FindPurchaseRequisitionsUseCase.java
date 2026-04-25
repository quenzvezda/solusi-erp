package com.solusi.erp.purchasing.purchaserequisition.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;

@FunctionalInterface
public interface FindPurchaseRequisitionsUseCase {
    Page<PurchaseRequisition> execute(String keyword, Pageable pageable);
}
