package com.solusi.erp.master.tax.application.usecase.query;

import com.solusi.erp.master.tax.domain.model.Tax;
import com.solusi.erp.master.tax.domain.repository.TaxRepository;

import java.util.Optional;

public class GetTaxEditViewUseCaseImpl implements GetTaxEditViewUseCase {

    private final TaxRepository repository;

    public GetTaxEditViewUseCaseImpl(TaxRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Tax> execute(Long id) {
        return repository.findById(id);
    }
}
