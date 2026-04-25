package com.solusi.erp.purchasing.purchaseorder.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;

public class FindPurchaseOrdersUseCaseImpl implements FindPurchaseOrdersUseCase {

    private final PurchaseOrderRepository repository;

    public FindPurchaseOrdersUseCaseImpl(PurchaseOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<PurchaseOrder> execute(String keyword, Pageable pageable) {
        return repository.findAll(keyword, pageable);
    }
}
