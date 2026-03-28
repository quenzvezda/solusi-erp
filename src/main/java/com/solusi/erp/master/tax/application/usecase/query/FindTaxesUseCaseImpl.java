package com.solusi.erp.master.tax.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.master.tax.domain.model.Tax;
import com.solusi.erp.master.tax.domain.repository.TaxRepository;

public class FindTaxesUseCaseImpl implements FindTaxesUseCase {

    private final TaxRepository repository;

    public FindTaxesUseCaseImpl(TaxRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Tax> execute(String keyword, Pageable pageable) {
        return repository.findAll(keyword, pageable);
    }
}
