package com.solusi.erp.master.bankaccount.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.master.bankaccount.domain.model.BankAccount;

@FunctionalInterface
public interface FindBankAccountsUseCase {
    Page<BankAccount> execute(String keyword, Pageable pageable);
}
