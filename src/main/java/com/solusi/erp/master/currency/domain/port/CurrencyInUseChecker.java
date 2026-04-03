package com.solusi.erp.master.currency.domain.port;

public interface CurrencyInUseChecker {
    boolean isInUse(Long currencyId);
}
