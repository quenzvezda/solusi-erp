package com.solusi.erp.accountspayable.debitmemo.application.usecase.command;

import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemo;

public interface CancelDebitMemoUseCase {

    DebitMemo execute(Long id);
}

