package com.solusi.erp.master.tax.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.tax.domain.model.Tax;
import com.solusi.erp.master.tax.domain.model.TaxCalculationMode;
import com.solusi.erp.master.tax.domain.repository.TaxRepository;

import java.math.BigDecimal;

public class CreateTaxUseCaseImpl implements CreateTaxUseCase {

    private final TaxRepository repository;

    public CreateTaxUseCaseImpl(TaxRepository repository) {
        this.repository = repository;
    }

    @Override
    public Tax execute(String code, String name, BigDecimal rate, String note,
                       Boolean isSubtract, Boolean isActive,
                       TaxCalculationMode calculationMode) {
        if (repository.existsByCode(code)) {
            throw new DomainException("msg.error.common.duplicate");
        }
        Tax tax = Tax.createNew(code, name, rate, note, isSubtract, isActive, calculationMode);
        return repository.save(tax);
    }
}
