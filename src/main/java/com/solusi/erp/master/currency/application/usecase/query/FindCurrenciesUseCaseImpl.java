package com.solusi.erp.master.currency.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.master.currency.domain.model.Currency;
import com.solusi.erp.master.currency.domain.repository.CurrencyRepository;

public class FindCurrenciesUseCaseImpl implements FindCurrenciesUseCase {

    private final CurrencyRepository repository;

    public FindCurrenciesUseCaseImpl(CurrencyRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Currency> execute(String keyword, Pageable pageable) {
        return repository.findAll(keyword, pageable);
    }
}
