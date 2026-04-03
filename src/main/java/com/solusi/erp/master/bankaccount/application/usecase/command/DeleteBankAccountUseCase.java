package com.solusi.erp.master.bankaccount.application.usecase.command;

import com.solusi.erp.core.domain.model.DeleteResult;

@FunctionalInterface
public interface DeleteBankAccountUseCase {
    DeleteResult execute(Long id);
}
