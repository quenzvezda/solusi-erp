package com.solusi.erp.purchasing.purchasereturn.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnStatus;
import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;

public class FindPurchaseReturnsUseCaseImpl implements FindPurchaseReturnsUseCase {

    private final PurchaseReturnRepository repository;

    public FindPurchaseReturnsUseCaseImpl(PurchaseReturnRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<PurchaseReturn> execute(String keyword, PurchaseReturnStatus status, Pageable pageable) {
        return repository.findAll(keyword, status, pageable);
    }
}
