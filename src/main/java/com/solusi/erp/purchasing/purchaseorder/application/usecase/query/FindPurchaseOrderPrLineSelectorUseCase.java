package com.solusi.erp.purchasing.purchaseorder.application.usecase.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FindPurchaseOrderPrLineSelectorUseCase {

    Page<PurchaseOrderPrLineSelectorRow> execute(Long prId, String keyword, List<Long> excludePrLineIds, Pageable pageable);
}
