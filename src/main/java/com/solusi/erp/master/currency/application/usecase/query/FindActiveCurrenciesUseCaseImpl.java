package com.solusi.erp.master.currency.application.usecase.query;

import com.solusi.erp.master.currency.domain.model.Currency;
import com.solusi.erp.master.currency.domain.repository.CurrencyRepository;

import java.util.List;

public class FindActiveCurrenciesUseCaseImpl implements FindActiveCurrenciesUseCase {

    private final CurrencyRepository repository;

    public FindActiveCurrenciesUseCaseImpl(CurrencyRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Currency> execute() {
        return repository.findByIsActiveTrue();
    }
}
