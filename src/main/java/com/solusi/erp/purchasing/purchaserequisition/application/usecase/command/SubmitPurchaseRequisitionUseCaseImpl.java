package com.solusi.erp.purchasing.purchaserequisition.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;
import com.solusi.erp.purchasing.purchaserequisition.domain.port.PurchaseRequisitionEventPublisher;
import com.solusi.erp.purchasing.purchaserequisition.domain.repository.PurchaseRequisitionRepository;

public class SubmitPurchaseRequisitionUseCaseImpl implements SubmitPurchaseRequisitionUseCase {

    private final PurchaseRequisitionRepository repository;
    private final PurchaseRequisitionEventPublisher eventPublisher;

    public SubmitPurchaseRequisitionUseCaseImpl(PurchaseRequisitionRepository repository,
                                                PurchaseRequisitionEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PurchaseRequisition execute(Long id, Long approverId) {
        PurchaseRequisition pr = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.pr.notfound"));

        pr.submit();
        PurchaseRequisition saved = repository.save(pr);

        eventPublisher.publishApprovalRequested(
                saved.getId(), saved.getCode(), saved.getRequesterId(), approverId
        );

        return saved;
    }
}
