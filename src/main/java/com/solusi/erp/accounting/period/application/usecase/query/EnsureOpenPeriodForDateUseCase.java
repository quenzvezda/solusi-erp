package com.solusi.erp.accounting.period.application.usecase.query;

import java.time.LocalDate;

@FunctionalInterface
public interface EnsureOpenPeriodForDateUseCase {
    void execute(LocalDate date);
}
