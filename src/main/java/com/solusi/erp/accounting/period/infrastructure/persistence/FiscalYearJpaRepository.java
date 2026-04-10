package com.solusi.erp.accounting.period.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FiscalYearJpaRepository extends JpaRepository<FiscalYear, Long> {

    @Query("SELECT f FROM FiscalYear f WHERE " +
            "(LOWER(f.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%')))" +
            " ORDER BY f.startDate DESC")
    Page<FiscalYear> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT f FROM FiscalYear f ORDER BY f.startDate DESC")
    Page<FiscalYear> findAllOrdered(Pageable pageable);

    Optional<FiscalYear> findByCode(String code);
}
