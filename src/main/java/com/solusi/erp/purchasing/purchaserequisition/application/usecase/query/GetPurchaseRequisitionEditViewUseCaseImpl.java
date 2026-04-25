package com.solusi.erp.purchasing.purchaserequisition.application.usecase.query;

import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;
import com.solusi.erp.purchasing.purchaserequisition.domain.repository.PurchaseRequisitionRepository;

import java.util.Optional;

public class GetPurchaseRequisitionEditViewUseCaseImpl implements GetPurchaseRequisitionEditViewUseCase {

    private final PurchaseRequisitionRepository repository;

    public GetPurchaseRequisitionEditViewUseCaseImpl(PurchaseRequisitionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PurchaseRequisition> execute(Long id) {
        return repository.findById(id);
    }
}
