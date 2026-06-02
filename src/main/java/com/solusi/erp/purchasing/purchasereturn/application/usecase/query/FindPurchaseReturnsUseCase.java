package com.solusi.erp.purchasing.purchasereturn.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnStatus;

public interface FindPurchaseReturnsUseCase {

    Page<PurchaseReturn> execute(String keyword, PurchaseReturnStatus status, Pageable pageable);
}
