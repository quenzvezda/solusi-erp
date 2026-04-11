package com.solusi.erp.accounting.period.application.usecase.command;

import com.solusi.erp.core.domain.model.DeleteResult;

@FunctionalInterface
public interface DeleteFiscalYearUseCase {
    DeleteResult execute(Long id);
}
