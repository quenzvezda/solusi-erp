package com.solusi.erp.master.currency.application.usecase.query;

import com.solusi.erp.master.currency.domain.model.Currency;
import com.solusi.erp.master.currency.domain.repository.CurrencyRepository;

import java.util.Optional;

public class GetCurrencyEditViewUseCaseImpl implements GetCurrencyEditViewUseCase {

    private final CurrencyRepository repository;

    public GetCurrencyEditViewUseCaseImpl(CurrencyRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Currency> execute(Long id) {
        return repository.findById(id);
    }
}
