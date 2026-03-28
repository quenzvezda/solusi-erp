package com.solusi.erp.master.bankaccount.application.usecase.query;

import com.solusi.erp.master.bankaccount.domain.model.BankAccount;

import java.util.Optional;

@FunctionalInterface
public interface GetBankAccountEditViewUseCase {
    Optional<BankAccount> execute(Long id);
}
