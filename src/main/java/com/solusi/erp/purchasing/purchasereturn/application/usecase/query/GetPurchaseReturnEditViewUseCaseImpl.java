package com.solusi.erp.purchasing.purchasereturn.application.usecase.query;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnSourceQueryPort;
import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;

import java.util.List;

public class GetPurchaseReturnEditViewUseCaseImpl implements GetPurchaseReturnEditViewUseCase {

    private final PurchaseReturnRepository repository;
    private final PurchaseReturnSourceQueryPort queryPort;

    public GetPurchaseReturnEditViewUseCaseImpl(PurchaseReturnRepository repository,
                                                PurchaseReturnSourceQueryPort queryPort) {
        this.repository = repository;
        this.queryPort = queryPort;
    }

    @Override
    public PurchaseReturnEditView execute(Long id) {
        PurchaseReturn purchaseReturn = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.purchase-return.not-found"));
        if (!purchaseReturn.getStatus().canEdit()) {
            throw new DomainException("msg.error.purchase-return.update.not-draft");
        }
        return new PurchaseReturnEditView(
                purchaseReturn,
                queryPort.findReturnableGrLineSlices(purchaseReturn.getReferenceId(), null, List.of())
        );
    }
}
