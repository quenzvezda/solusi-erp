package com.solusi.erp.accounting.period.application.usecase.query;

import com.solusi.erp.accounting.period.domain.model.AccountingPeriod;
import com.solusi.erp.accounting.period.domain.model.FiscalYear;
import com.solusi.erp.accounting.period.domain.repository.FiscalYearRepository;

import java.util.List;
import java.util.Optional;

public class GetFiscalYearDetailUseCaseImpl implements GetFiscalYearDetailUseCase {

    private final FiscalYearRepository repository;

    public GetFiscalYearDetailUseCaseImpl(FiscalYearRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<FiscalYear> execute(Long id) {
        Optional<FiscalYear> fyOpt = repository.findById(id);
        if (fyOpt.isPresent()) {
            FiscalYear fy = fyOpt.get();
            List<AccountingPeriod> periods = repository.findPeriodsByFiscalYearId(id);
            // Reconstruct FY with periods for detail view
            return Optional.of(new FiscalYear(
                    fy.getMetadata(), fy.getCode(), fy.getName(),
                    fy.getStartDate(), fy.getEndDate(), fy.getIsActive(), periods));
        }
        return Optional.empty();
    }
}
