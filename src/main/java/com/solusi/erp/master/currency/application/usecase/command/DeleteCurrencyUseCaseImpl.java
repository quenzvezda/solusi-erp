package com.solusi.erp.master.currency.application.usecase.command;

import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.currency.domain.model.Currency;
import com.solusi.erp.master.currency.domain.port.CurrencyInUseChecker;
import com.solusi.erp.master.currency.domain.repository.CurrencyRepository;

public class DeleteCurrencyUseCaseImpl implements DeleteCurrencyUseCase {

    private final CurrencyRepository repository;
    private final CurrencyInUseChecker inUseChecker;

    public DeleteCurrencyUseCaseImpl(CurrencyRepository repository, CurrencyInUseChecker inUseChecker) {
        this.repository = repository;
        this.inUseChecker = inUseChecker;
    }

    @Override
    public DeleteResult execute(Long id) {
        Currency currency = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.currency.notfound"));

        if (inUseChecker.isInUse(id)) {
            currency.softDelete();
            repository.save(currency);
            return DeleteResult.SOFT_DELETED;
        }

        repository.delete(id);
        return DeleteResult.HARD_DELETED;
    }
}
