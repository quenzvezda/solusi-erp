package com.solusi.erp.master.bankaccount.infrastructure.adapter;

import com.solusi.erp.master.bankaccount.domain.port.BankAccountInUseChecker;

public class BankAccountInUseCheckerImpl implements BankAccountInUseChecker {

    public BankAccountInUseCheckerImpl() {
    }

    @Override
    public boolean isInUse(Long bankAccountId) {
        return false;
    }
}
