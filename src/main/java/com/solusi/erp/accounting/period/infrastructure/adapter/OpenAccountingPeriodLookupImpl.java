package com.solusi.erp.accounting.period.infrastructure.adapter;

import com.solusi.erp.accounting.period.domain.model.AccountingPeriod;
import com.solusi.erp.accounting.period.domain.port.OpenAccountingPeriodLookup;
import com.solusi.erp.accounting.period.infrastructure.persistence.AccountingPeriodJpaRepository;
import com.solusi.erp.accounting.period.infrastructure.persistence.PeriodPersistenceMapper;

import java.time.LocalDate;
import java.util.Optional;

public class OpenAccountingPeriodLookupImpl implements OpenAccountingPeriodLookup {

    private final AccountingPeriodJpaRepository accountingPeriodJpaRepository;
    private final PeriodPersistenceMapper periodPersistenceMapper;

    public OpenAccountingPeriodLookupImpl(AccountingPeriodJpaRepository accountingPeriodJpaRepository,
                                          PeriodPersistenceMapper periodPersistenceMapper) {
        this.accountingPeriodJpaRepository = accountingPeriodJpaRepository;
        this.periodPersistenceMapper = periodPersistenceMapper;
    }

    @Override
    public Optional<AccountingPeriod> findOpenPeriodContaining(LocalDate date) {
        return accountingPeriodJpaRepository.findOpenPeriodContaining(date)
                .map(periodPersistenceMapper::toDomain);
    }
}
