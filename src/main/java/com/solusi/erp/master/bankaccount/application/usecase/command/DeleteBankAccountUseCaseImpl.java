package com.solusi.erp.master.bankaccount.application.usecase.command;

import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.bankaccount.domain.model.BankAccount;
import com.solusi.erp.master.bankaccount.domain.port.BankAccountInUseChecker;
import com.solusi.erp.master.bankaccount.domain.repository.BankAccountRepository;

public class DeleteBankAccountUseCaseImpl implements DeleteBankAccountUseCase {

    private final BankAccountRepository repository;
    private final BankAccountInUseChecker inUseChecker;

    public DeleteBankAccountUseCaseImpl(BankAccountRepository repository, BankAccountInUseChecker inUseChecker) {
        this.repository = repository;
        this.inUseChecker = inUseChecker;
    }

    @Override
    public DeleteResult execute(Long id) {
        BankAccount bankAccount = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.bank-account.notfound"));

        if (inUseChecker.isInUse(id)) {
            bankAccount.softDelete();
            repository.save(bankAccount);
            return DeleteResult.SOFT_DELETED;
        }

        repository.delete(id);
        return DeleteResult.HARD_DELETED;
    }
}
