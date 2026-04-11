package com.solusi.erp.purchasing.purchaserequisition.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;
import com.solusi.erp.purchasing.purchaserequisition.domain.repository.PurchaseRequisitionRepository;

public class DeletePurchaseRequisitionUseCaseImpl implements DeletePurchaseRequisitionUseCase {

    private final PurchaseRequisitionRepository repository;

    public DeletePurchaseRequisitionUseCaseImpl(PurchaseRequisitionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(Long id) {
        PurchaseRequisition pr = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.pr.notfound"));
        pr.deactivate();
        repository.save(pr);
    }
}
