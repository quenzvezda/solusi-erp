package com.solusi.erp.purchasing.purchasereturn.application.usecase.command;

import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;

public interface CancelApprovedPurchaseReturnUseCase {

    PurchaseReturn execute(Long id);
}
