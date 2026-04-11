package com.solusi.erp.accounting.period.application.usecase.command;

import com.solusi.erp.accounting.period.domain.model.FiscalYear;

import java.time.LocalDate;

@FunctionalInterface
public interface CreateFiscalYearUseCase {
    FiscalYear execute(String name, LocalDate startDate, LocalDate endDate, Boolean isActive);
}
