package com.solusi.erp.accounting.period.application.usecase.command;

import com.solusi.erp.accounting.period.domain.model.FiscalYear;
import com.solusi.erp.accounting.period.domain.repository.FiscalYearRepository;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;

import java.time.LocalDate;

public class CreateFiscalYearUseCaseImpl implements CreateFiscalYearUseCase {

    private final FiscalYearRepository repository;
    private final SequenceGeneratorService sequenceGenerator;

    public CreateFiscalYearUseCaseImpl(FiscalYearRepository repository,
                                       SequenceGeneratorService sequenceGenerator) {
        this.repository = repository;
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public FiscalYear execute(String name, LocalDate startDate, LocalDate endDate, Boolean isActive) {
        String code = sequenceGenerator.generate("FISCAL_YEAR");

        if (repository.existsByCode(code)) {
            throw new DomainException("msg.error.period.code.exists");
        }

        FiscalYear fy = FiscalYear.createNew(code, name, startDate, endDate, isActive);
        FiscalYear saved = repository.save(fy);

        // Auto-generate 12 monthly periods
        var periods = saved.generateMonthlyPeriods(saved.getId());
        var savedPeriods = repository.savePeriods(periods);

        // Reconstruct FY with persisted periods (they now have DB IDs)
        return new FiscalYear(saved.getMetadata(), saved.getCode(), saved.getName(),
                saved.getStartDate(), saved.getEndDate(), saved.getIsActive(), savedPeriods);
    }
}
