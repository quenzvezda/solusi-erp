package com.solusi.erp.master.currency.application.usecase.query;

import com.solusi.erp.master.currency.domain.model.Currency;

import java.util.Optional;

@FunctionalInterface
public interface GetCurrencyEditViewUseCase {
    Optional<Currency> execute(Long id);
}
