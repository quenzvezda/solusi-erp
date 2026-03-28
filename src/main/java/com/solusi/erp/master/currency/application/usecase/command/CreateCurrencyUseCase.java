package com.solusi.erp.master.currency.application.usecase.command;

import com.solusi.erp.master.currency.domain.model.Currency;

@FunctionalInterface
public interface CreateCurrencyUseCase {
    Currency execute(String symbol, String alias, String name, String note, Boolean isDefault, Boolean isActive);
}
