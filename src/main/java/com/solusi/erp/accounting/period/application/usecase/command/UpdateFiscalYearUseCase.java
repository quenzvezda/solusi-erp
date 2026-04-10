package com.solusi.erp.accounting.period.application.usecase.command;

import com.solusi.erp.accounting.period.domain.model.FiscalYear;

@FunctionalInterface
public interface UpdateFiscalYearUseCase {
    FiscalYear execute(Long id, String name, Boolean isActive);
}
