package com.solusi.erp.master.tax.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.tax.domain.model.Tax;
import com.solusi.erp.master.tax.domain.repository.TaxRepository;

public class DeleteTaxUseCaseImpl implements DeleteTaxUseCase {

    private final TaxRepository repository;

    public DeleteTaxUseCaseImpl(TaxRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(Long id) {
        Tax tax = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.tax.notfound"));
        tax.softDelete();
        repository.save(tax);
    }
}
