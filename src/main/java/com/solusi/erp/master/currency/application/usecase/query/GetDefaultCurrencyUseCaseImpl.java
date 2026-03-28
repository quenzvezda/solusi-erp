package com.solusi.erp.master.currency.application.usecase.query;

import com.solusi.erp.master.currency.domain.model.Currency;
import com.solusi.erp.master.currency.domain.repository.CurrencyRepository;

import java.util.Optional;

public class GetDefaultCurrencyUseCaseImpl implements GetDefaultCurrencyUseCase {

    private final CurrencyRepository repository;

    public GetDefaultCurrencyUseCaseImpl(CurrencyRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Currency> execute() {
        return repository.findByIsDefaultTrue().stream().findFirst();
    }
}
