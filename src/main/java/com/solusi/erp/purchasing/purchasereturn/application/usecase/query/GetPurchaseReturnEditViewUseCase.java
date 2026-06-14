package com.solusi.erp.purchasing.purchasereturn.application.usecase.query;

import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;

import java.util.List;

public interface GetPurchaseReturnEditViewUseCase {

    PurchaseReturnEditView execute(Long id);

    record PurchaseReturnEditView(
            PurchaseReturn purchaseReturn,
            List<ReturnableGrLineSlice> availableSlices
    ) {
    }
}
