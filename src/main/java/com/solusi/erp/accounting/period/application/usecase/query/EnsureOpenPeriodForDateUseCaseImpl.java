package com.solusi.erp.accounting.period.application.usecase.query;

import com.solusi.erp.accounting.period.domain.port.OpenAccountingPeriodLookup;
import com.solusi.erp.core.exception.DomainException;

import java.time.LocalDate;

public class EnsureOpenPeriodForDateUseCaseImpl implements EnsureOpenPeriodForDateUseCase {

    private final OpenAccountingPeriodLookup openAccountingPeriodLookup;

    public EnsureOpenPeriodForDateUseCaseImpl(OpenAccountingPeriodLookup openAccountingPeriodLookup) {
        this.openAccountingPeriodLookup = openAccountingPeriodLookup;
    }

    @Override
    public void execute(LocalDate date) {
        openAccountingPeriodLookup.findOpenPeriodContaining(date)
                .orElseThrow(() -> new DomainException("msg.error.period.not.open"));
    }
}
