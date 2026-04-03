package com.solusi.erp.master.currency.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.currency.domain.model.Currency;
import com.solusi.erp.master.currency.domain.repository.CurrencyRepository;

import java.util.List;

public class CreateCurrencyUseCaseImpl implements CreateCurrencyUseCase {

    private final CurrencyRepository repository;

    public CreateCurrencyUseCaseImpl(CurrencyRepository repository) {
        this.repository = repository;
    }

    @Override
    public Currency execute(String symbol, String alias, String name, String note,
                            Boolean isDefault, Boolean isActive) {
        if (repository.existsByAlias(alias)) {
            throw new DomainException("msg.error.common.duplicate");
        }
        if (Boolean.TRUE.equals(isDefault)) {
            List<Currency> defaults = repository.findByIsDefaultTrue();
            for (Currency prev : defaults) {
                prev.unsetDefault();
                repository.save(prev);
            }
        }
        Currency currency = Currency.createNew(symbol, alias, name, note, isDefault, isActive);
        return repository.save(currency);
    }
}
