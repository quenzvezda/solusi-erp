package com.solusi.erp.master.currency.application.usecase.command;

import com.solusi.erp.master.currency.domain.model.Currency;

@FunctionalInterface
public interface UpdateCurrencyUseCase {
    Currency execute(Long id, String symbol, String name, String note, Boolean isDefault, Boolean isActive);
}
