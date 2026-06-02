package com.solusi.erp.purchasing.purchasereturn.application.usecase.command;

import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;

public interface ConfirmPurchaseReturnUseCase {

    PurchaseReturn execute(Long id);
}
