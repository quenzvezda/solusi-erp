package com.solusi.erp.accounting.period.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountingPeriodJpaRepository extends JpaRepository<AccountingPeriod, Long> {

    @Query("SELECT p FROM AccountingPeriod p WHERE p.fiscalYearId = :fyId ORDER BY p.startDate ASC")
    List<AccountingPeriod> findByFiscalYearIdOrderByStartDate(@Param("fyId") Long fiscalYearId);

    @Query("SELECT p FROM AccountingPeriod p WHERE p.status = 'OPEN' AND :date BETWEEN p.startDate AND p.endDate")
    Optional<AccountingPeriod> findOpenPeriodContaining(@Param("date") LocalDate date);

    boolean existsByFiscalYearId(Long fiscalYearId);
}
