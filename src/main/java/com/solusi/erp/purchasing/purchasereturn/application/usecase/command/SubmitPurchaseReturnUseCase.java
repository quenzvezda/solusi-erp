package com.solusi.erp.purchasing.purchasereturn.application.usecase.command;

import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;

public interface SubmitPurchaseReturnUseCase {

    PurchaseReturn execute(Long id, Long submitterUserId, Long requesterPartyId, Long approverId);
}
