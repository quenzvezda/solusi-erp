package com.solusi.erp.accounting.period.application.usecase.command;

import com.solusi.erp.accounting.period.domain.model.AccountingPeriod;

@FunctionalInterface
public interface ReopenPeriodUseCase {
    AccountingPeriod execute(Long periodId);
}
