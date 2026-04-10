package com.solusi.erp.accounting.period.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.accounting.period.domain.model.AccountingPeriod;
import com.solusi.erp.accounting.period.domain.model.FiscalYear;
import com.solusi.erp.accounting.period.domain.repository.FiscalYearRepository;

public class ReopenPeriodUseCaseImpl implements ReopenPeriodUseCase {

    private final FiscalYearRepository repository;

    public ReopenPeriodUseCaseImpl(FiscalYearRepository repository) {
        this.repository = repository;
    }

    @Override
    public AccountingPeriod execute(Long periodId) {
        FiscalYear fy = repository.findByPeriodId(periodId)
                .orElseThrow(() -> new DomainException("msg.error.period.notfound"));
        AccountingPeriod period = fy.reopenPeriod(periodId);
        return repository.savePeriod(period);
    }
}
