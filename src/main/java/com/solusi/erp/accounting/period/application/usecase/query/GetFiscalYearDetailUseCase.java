package com.solusi.erp.accounting.period.application.usecase.query;

import com.solusi.erp.accounting.period.domain.model.FiscalYear;

import java.util.Optional;

@FunctionalInterface
public interface GetFiscalYearDetailUseCase {
    Optional<FiscalYear> execute(Long id);
}
