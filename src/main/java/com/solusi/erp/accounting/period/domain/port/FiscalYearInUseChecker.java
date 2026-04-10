package com.solusi.erp.accounting.period.domain.port;

public interface FiscalYearInUseChecker {
    boolean isInUse(Long fiscalYearId);
}
