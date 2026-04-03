package com.solusi.erp.master.currency.infrastructure.adapter;

import com.solusi.erp.master.currency.domain.port.CurrencyInUseChecker;

public class CurrencyInUseCheckerImpl implements CurrencyInUseChecker {

    public CurrencyInUseCheckerImpl() {
    }

    @Override
    public boolean isInUse(Long currencyId) {
        return false;
    }
}
