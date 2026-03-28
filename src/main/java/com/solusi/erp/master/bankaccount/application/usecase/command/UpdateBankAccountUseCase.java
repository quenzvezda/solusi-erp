package com.solusi.erp.master.bankaccount.application.usecase.command;

import com.solusi.erp.master.bankaccount.domain.model.BankAccount;

@FunctionalInterface
public interface UpdateBankAccountUseCase {
    BankAccount execute(Long id, String bankName, String branch, String accountName, String accountNo,
                        String accountType, String note, Long cityId, Long partyId, Boolean isActive);
}
