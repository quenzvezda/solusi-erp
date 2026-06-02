package com.solusi.erp.purchasing.purchasereturn.application.usecase.command;

import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;

public interface CancelDraftPurchaseReturnUseCase {

    PurchaseReturn execute(Long id);
}
