package com.solusi.erp.accountspayable.debitmemo.application.usecase.query;

import java.util.Optional;

public interface FindDebitMemoByPurchaseReturnUseCase {

    Optional<DebitMemoSourceLinkView> execute(Long purchaseReturnId);
}

