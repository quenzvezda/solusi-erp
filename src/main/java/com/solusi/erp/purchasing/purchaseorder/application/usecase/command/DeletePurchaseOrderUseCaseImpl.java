package com.solusi.erp.purchasing.purchaseorder.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;

public class DeletePurchaseOrderUseCaseImpl implements DeletePurchaseOrderUseCase {

    private final PurchaseOrderRepository repository;

    public DeletePurchaseOrderUseCaseImpl(PurchaseOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(Long id) {
        PurchaseOrder po = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.po.notfound"));
        po.deactivate();
        repository.save(po);
    }
}
