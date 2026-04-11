package com.solusi.erp.purchasing.purchaseorder.application.usecase.query;

import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;

import java.util.Optional;

public class GetPurchaseOrderEditViewUseCaseImpl implements GetPurchaseOrderEditViewUseCase {

    private final PurchaseOrderRepository repository;

    public GetPurchaseOrderEditViewUseCaseImpl(PurchaseOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PurchaseOrder> execute(Long id) {
        return repository.findById(id);
    }
}
