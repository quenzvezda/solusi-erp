package com.solusi.erp.master.currency.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.currency.domain.model.Currency;
import com.solusi.erp.master.currency.domain.repository.CurrencyRepository;

public class DeleteCurrencyUseCaseImpl implements DeleteCurrencyUseCase {

    private final CurrencyRepository repository;

    public DeleteCurrencyUseCaseImpl(CurrencyRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(Long id) {
        Currency currency = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.currency.notfound"));
        currency.softDelete();
        repository.save(currency);
    }
}
