package com.solusi.erp.master.bankaccount.application.usecase.query;

import com.solusi.erp.master.bankaccount.domain.model.BankAccount;
import com.solusi.erp.master.bankaccount.domain.repository.BankAccountRepository;

import java.util.Optional;

public class GetBankAccountEditViewUseCaseImpl implements GetBankAccountEditViewUseCase {

    private final BankAccountRepository repository;

    public GetBankAccountEditViewUseCaseImpl(BankAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<BankAccount> execute(Long id) {
        return repository.findById(id);
    }
}
