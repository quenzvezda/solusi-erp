package com.solusi.erp.accounting.period.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.accounting.period.domain.model.AccountingPeriod;
import com.solusi.erp.accounting.period.domain.model.FiscalYear;

import java.util.List;
import java.util.Optional;

public interface FiscalYearRepository {
    FiscalYear save(FiscalYear fiscalYear);
    Optional<FiscalYear> findById(Long id);
    Optional<FiscalYear> findByPeriodId(Long periodId);
    Page<FiscalYear> findAll(String keyword, Pageable pageable);
    void delete(Long id);
    boolean existsByCode(String code);

    AccountingPeriod savePeriod(AccountingPeriod period);
    List<AccountingPeriod> savePeriods(List<AccountingPeriod> periods);
    List<AccountingPeriod> findPeriodsByFiscalYearId(Long fiscalYearId);
}
