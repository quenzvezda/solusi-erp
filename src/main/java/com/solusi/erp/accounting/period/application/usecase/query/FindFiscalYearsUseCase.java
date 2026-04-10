package com.solusi.erp.accounting.period.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.accounting.period.domain.model.FiscalYear;

@FunctionalInterface
public interface FindFiscalYearsUseCase {
    Page<FiscalYear> execute(String keyword, Pageable pageable);
}
