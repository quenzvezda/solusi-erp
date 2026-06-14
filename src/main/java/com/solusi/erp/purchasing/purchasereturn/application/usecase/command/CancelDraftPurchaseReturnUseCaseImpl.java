package com.solusi.erp.purchasing.purchasereturn.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;

public class CancelDraftPurchaseReturnUseCaseImpl implements CancelDraftPurchaseReturnUseCase {

    private final PurchaseReturnRepository repository;

    public CancelDraftPurchaseReturnUseCaseImpl(PurchaseReturnRepository repository) {
        this.repository = repository;
    }

    @Override
    public PurchaseReturn execute(Long id) {
        PurchaseReturn purchaseReturn = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.purchase-return.not-found"));
        purchaseReturn.cancelDraft();
        return repository.save(purchaseReturn);
    }
}
