package com.solusi.erp.master.bankaccount.application.usecase.command;

import com.solusi.erp.master.bankaccount.domain.model.BankAccount;
import com.solusi.erp.master.shared.model.PaymentType;

@FunctionalInterface
public interface CreateBankAccountUseCase {
    BankAccount execute(String bankName, String branch, String accountName, String accountNo,
                        PaymentType accountType, String note, Long cityId, Long partyId,
                        Boolean isActive, Long currencyId, Long coaId);
}
