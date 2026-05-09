package com.solusi.erp.accounting.period.domain.port;

import com.solusi.erp.accounting.period.domain.model.AccountingPeriod;

import java.time.LocalDate;
import java.util.Optional;

public interface OpenAccountingPeriodLookup {
    Optional<AccountingPeriod> findOpenPeriodContaining(LocalDate date);
}
