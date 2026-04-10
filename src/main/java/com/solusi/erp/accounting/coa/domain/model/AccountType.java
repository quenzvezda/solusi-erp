package com.solusi.erp.accounting.coa.domain.model;

public enum AccountType {
    ASSET(NormalBalance.DEBIT),
    LIABILITY(NormalBalance.CREDIT),
    EQUITY(NormalBalance.CREDIT),
    REVENUE(NormalBalance.CREDIT),
    EXPENSE(NormalBalance.DEBIT);

    private final NormalBalance defaultNormalBalance;

    AccountType(NormalBalance defaultNormalBalance) {
        this.defaultNormalBalance = defaultNormalBalance;
    }

    public NormalBalance getDefaultNormalBalance() {
        return defaultNormalBalance;
    }
}
