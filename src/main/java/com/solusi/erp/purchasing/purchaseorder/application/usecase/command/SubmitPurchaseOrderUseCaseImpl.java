package com.solusi.erp.purchasing.purchaseorder.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.port.PurchaseOrderEventPublisher;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;

public class SubmitPurchaseOrderUseCaseImpl implements SubmitPurchaseOrderUseCase {

    private final PurchaseOrderRepository repository;
    private final PurchaseOrderEventPublisher eventPublisher;

    public SubmitPurchaseOrderUseCaseImpl(PurchaseOrderRepository repository,
                                          PurchaseOrderEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PurchaseOrder execute(Long id, Long approverId) {
        PurchaseOrder po = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.po.notfound"));

        po.submit();
        PurchaseOrder saved = repository.save(po);

        eventPublisher.publishApprovalRequested(
                saved.getId(), saved.getCode(),
                "/purchasing/purchase-orders/view/" + saved.getId(),
                saved.getSupplierId(), approverId
        );

        return saved;
    }
}
