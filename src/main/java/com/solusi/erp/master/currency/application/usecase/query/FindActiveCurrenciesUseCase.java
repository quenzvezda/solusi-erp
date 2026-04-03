package com.solusi.erp.master.currency.application.usecase.query;

import com.solusi.erp.master.currency.domain.model.Currency;

import java.util.List;

@FunctionalInterface
public interface FindActiveCurrenciesUseCase {
    List<Currency> execute();
}
