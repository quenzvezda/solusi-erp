package com.solusi.erp.accounting.period.application.usecase.command;

import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.accounting.period.domain.model.FiscalYear;
import com.solusi.erp.accounting.period.domain.port.FiscalYearInUseChecker;
import com.solusi.erp.accounting.period.domain.repository.FiscalYearRepository;

public class DeleteFiscalYearUseCaseImpl implements DeleteFiscalYearUseCase {

    private final FiscalYearRepository repository;
    private final FiscalYearInUseChecker inUseChecker;

    public DeleteFiscalYearUseCaseImpl(FiscalYearRepository repository,
                                       FiscalYearInUseChecker inUseChecker) {
        this.repository = repository;
        this.inUseChecker = inUseChecker;
    }

    @Override
    public DeleteResult execute(Long id) {
        FiscalYear fy = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.period.notfound"));
        if (inUseChecker.isInUse(id)) {
            fy.softDelete();
            repository.save(fy);
            return DeleteResult.SOFT_DELETED;
        }
        repository.delete(id);
        return DeleteResult.HARD_DELETED;
    }
}
