package com.solusi.erp.accounting.period.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.accounting.period.domain.model.FiscalYear;
import com.solusi.erp.accounting.period.domain.repository.FiscalYearRepository;

public class FindFiscalYearsUseCaseImpl implements FindFiscalYearsUseCase {

    private final FiscalYearRepository repository;

    public FindFiscalYearsUseCaseImpl(FiscalYearRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<FiscalYear> execute(String keyword, Pageable pageable) {
        return repository.findAll(keyword, pageable);
    }
}
