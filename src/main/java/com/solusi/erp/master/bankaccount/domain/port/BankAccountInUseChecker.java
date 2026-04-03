package com.solusi.erp.master.bankaccount.domain.port;

public interface BankAccountInUseChecker {
    boolean isInUse(Long bankAccountId);
}
