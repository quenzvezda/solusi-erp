package com.solusi.erp.master.bankaccount.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.bankaccount.domain.model.BankAccount;
import com.solusi.erp.master.bankaccount.domain.repository.BankAccountRepository;

public class UpdateBankAccountUseCaseImpl implements UpdateBankAccountUseCase {

    private final BankAccountRepository repository;

    public UpdateBankAccountUseCaseImpl(BankAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public BankAccount execute(Long id, String bankName, String branch, String accountName,
                               String accountNo, String accountType, String note,
                               Long cityId, Long partyId, Boolean isActive) {
        BankAccount existing = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.bank-account.notfound"));
        return repository.save(new BankAccount(existing.getMetadata(), existing.getCode(),
                bankName, branch, accountName, accountNo, accountType, note,
                cityId, existing.getCityName(), partyId, existing.getPartyName(), isActive));
    }
}
