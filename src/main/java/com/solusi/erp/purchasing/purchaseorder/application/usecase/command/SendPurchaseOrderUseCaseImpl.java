package com.solusi.erp.purchasing.purchaseorder.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;

public class SendPurchaseOrderUseCaseImpl implements SendPurchaseOrderUseCase {

    private final PurchaseOrderRepository repository;

    public SendPurchaseOrderUseCaseImpl(PurchaseOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public PurchaseOrder execute(Long id) {
        PurchaseOrder po = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.po.notfound"));
        po.send();
        return repository.save(po);
    }
}
