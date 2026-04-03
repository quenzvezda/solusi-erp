package com.solusi.erp.master.currency.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.currency.domain.model.Currency;
import com.solusi.erp.master.currency.domain.repository.CurrencyRepository;

import java.util.List;

public class UpdateCurrencyUseCaseImpl implements UpdateCurrencyUseCase {

    private final CurrencyRepository repository;

    public UpdateCurrencyUseCaseImpl(CurrencyRepository repository) {
        this.repository = repository;
    }

    @Override
    public Currency execute(Long id, String symbol, String name, String note,
                            Boolean isDefault, Boolean isActive) {
        Currency currency = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.currency.notfound"));
        if (Boolean.TRUE.equals(isDefault)) {
            List<Currency> defaults = repository.findByIsDefaultTrue();
            for (Currency prev : defaults) {
                if (!prev.getId().equals(id)) {
                    prev.unsetDefault();
                    repository.save(prev);
                }
            }
        }
        currency.update(symbol, name, note, isDefault, isActive);
        return repository.save(currency);
    }
}
