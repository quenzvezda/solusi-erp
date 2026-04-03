package com.solusi.erp.master.currency.application.usecase.query;

import com.solusi.erp.master.currency.domain.model.Currency;

import java.util.Optional;

@FunctionalInterface
public interface GetDefaultCurrencyUseCase {
    Optional<Currency> execute();
}
