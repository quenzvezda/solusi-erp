package com.solusi.erp.master.tax.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.tax.domain.model.Tax;
import com.solusi.erp.master.tax.domain.repository.TaxRepository;

import java.math.BigDecimal;

public class UpdateTaxUseCaseImpl implements UpdateTaxUseCase {

    private final TaxRepository repository;

    public UpdateTaxUseCaseImpl(TaxRepository repository) {
        this.repository = repository;
    }

    @Override
    public Tax execute(Long id, String name, BigDecimal rate, String note,
                       Boolean isSubtract, Boolean isActive) {
        Tax tax = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.tax.notfound"));
        tax.update(name, rate, note, isSubtract, isActive);
        return repository.save(tax);
    }
}
