package com.solusi.erp.purchasing.purchasereturn.application.usecase.query;

import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;

import java.util.Optional;

public class GetPurchaseReturnUseCaseImpl implements GetPurchaseReturnUseCase {

    private final PurchaseReturnRepository repository;

    public GetPurchaseReturnUseCaseImpl(PurchaseReturnRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PurchaseReturn> execute(Long id) {
        return repository.findById(id);
    }
}
