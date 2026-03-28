package com.solusi.erp.master.bankaccount.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.bankaccount.domain.repository.BankAccountRepository;

public class DeleteBankAccountUseCaseImpl implements DeleteBankAccountUseCase {

    private final BankAccountRepository repository;

    public DeleteBankAccountUseCaseImpl(BankAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(Long id) {
        repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.bank-account.notfound"));
        repository.delete(id);
    }
}
