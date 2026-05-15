package com.solusi.erp.master.bankaccount.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.bankaccount.domain.model.BankAccount;
import com.solusi.erp.master.bankaccount.domain.repository.BankAccountRepository;
import com.solusi.erp.master.shared.model.PaymentType;

public class UpdateBankAccountUseCaseImpl implements UpdateBankAccountUseCase {

    private final BankAccountRepository repository;

    public UpdateBankAccountUseCaseImpl(BankAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public BankAccount execute(Long id, String bankName, String branch, String accountName,
                               String accountNo, PaymentType accountType, String note,
                               Long cityId, Long partyId, Boolean isActive,
                               Long currencyId, Long coaId) {
        BankAccount existing = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.bank-account.notfound"));
        existing.update(bankName, branch, accountName, accountNo, accountType, note,
                cityId, partyId, isActive, currencyId, coaId);
        return repository.save(existing);
    }
}
