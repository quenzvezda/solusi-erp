package com.solusi.erp.purchasing.purchaserequisition.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;
import com.solusi.erp.purchasing.purchaserequisition.domain.repository.PurchaseRequisitionRepository;

public class FindPurchaseRequisitionsUseCaseImpl implements FindPurchaseRequisitionsUseCase {

    private final PurchaseRequisitionRepository repository;

    public FindPurchaseRequisitionsUseCaseImpl(PurchaseRequisitionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<PurchaseRequisition> execute(String keyword, Pageable pageable) {
        return repository.findAll(keyword, pageable);
    }
}
