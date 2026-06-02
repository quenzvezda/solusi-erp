package com.solusi.erp.purchasing.purchasereturn.application.usecase.query;

import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;

import java.util.Optional;

public interface GetPurchaseReturnUseCase {

    Optional<PurchaseReturn> execute(Long id);
}
