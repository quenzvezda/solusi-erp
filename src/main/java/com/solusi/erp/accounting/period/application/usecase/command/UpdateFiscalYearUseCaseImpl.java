package com.solusi.erp.accounting.period.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.accounting.period.domain.model.FiscalYear;
import com.solusi.erp.accounting.period.domain.repository.FiscalYearRepository;

public class UpdateFiscalYearUseCaseImpl implements UpdateFiscalYearUseCase {

    private final FiscalYearRepository repository;

    public UpdateFiscalYearUseCaseImpl(FiscalYearRepository repository) {
        this.repository = repository;
    }

    @Override
    public FiscalYear execute(Long id, String name, Boolean isActive) {
        FiscalYear fy = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.period.notfound"));
        fy.update(name, isActive);
        return repository.save(fy);
    }
}
