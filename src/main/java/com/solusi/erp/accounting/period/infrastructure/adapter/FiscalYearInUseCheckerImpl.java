package com.solusi.erp.accounting.period.infrastructure.adapter;

import com.solusi.erp.accounting.period.domain.port.FiscalYearInUseChecker;

public class FiscalYearInUseCheckerImpl implements FiscalYearInUseChecker {

    public FiscalYearInUseCheckerImpl() {
    }

    @Override
    public boolean isInUse(Long fiscalYearId) {
        // Placeholder — will check JournalEntry references when Journal module is built
        return false;
    }
}
