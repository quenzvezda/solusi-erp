package com.solusi.erp.master.bankaccount.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.master.bankaccount.domain.model.BankAccount;
import com.solusi.erp.master.bankaccount.domain.repository.BankAccountRepository;

public class FindBankAccountsUseCaseImpl implements FindBankAccountsUseCase {

    private final BankAccountRepository repository;

    public FindBankAccountsUseCaseImpl(BankAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<BankAccount> execute(String keyword, Pageable pageable) {
        return repository.findAll(keyword, pageable);
    }
}
