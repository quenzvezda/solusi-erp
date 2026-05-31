package com.solusi.erp.master.currency.domain.port;

public interface CurrencyPostingValidator {
    CurrencyPostingInfo getPostingInfo(Long currencyId);

    record CurrencyPostingInfo(boolean active, boolean defaultCurrency) {
    }
}
