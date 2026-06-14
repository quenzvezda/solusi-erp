package com.solusi.erp.accountspayable.debitmemo.application.usecase.command;

import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemo;

public interface CreateDebitMemoFromPurchaseReturnUseCase {

    DebitMemo execute(DebitMemoPurchaseReturnSource source);
}

