package com.solusi.erp.purchasing.purchaseorder.application.usecase.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FindPurchaseOrderPrSelectorUseCase {

    Page<PurchaseOrderPrSelectorRow> execute(String keyword, Long supplierId, Pageable pageable);
}
