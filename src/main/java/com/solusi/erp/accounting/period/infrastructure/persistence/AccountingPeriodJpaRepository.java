package com.solusi.erp.accounting.period.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountingPeriodJpaRepository extends JpaRepository<AccountingPeriod, Long> {

    @Query("SELECT p FROM AccountingPeriod p WHERE p.fiscalYearId = :fyId ORDER BY p.startDate ASC")
    List<AccountingPeriod> findByFiscalYearIdOrderByStartDate(@Param("fyId") Long fiscalYearId);

    boolean existsByFiscalYearId(Long fiscalYearId);
}
