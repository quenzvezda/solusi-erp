package com.solusi.erp.purchasing.purchasereturn.application.usecase.query;

import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;

import java.util.List;

public interface GetPurchaseReturnReverseViewUseCase {

    PurchaseReturnReverseView execute(Long id);

    record PurchaseReturnReverseView(
            PurchaseReturn purchaseReturn,
            List<PurchaseReturnReverseLineView> lines
    ) {
    }
}
