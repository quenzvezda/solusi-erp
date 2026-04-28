package com.solusi.erp.accounting.coa.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoaJpaRepository extends JpaRepository<ChartOfAccount, Long> {

    @Query("SELECT c FROM ChartOfAccount c WHERE " +
            "(LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))" +
            " ORDER BY c.code ASC")
    Page<ChartOfAccount> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT c FROM ChartOfAccount c ORDER BY c.code ASC")
    Page<ChartOfAccount> findAllOrdered(Pageable pageable);

    Optional<ChartOfAccount> findByCode(String code);

    @Query("SELECT c FROM ChartOfAccount c WHERE c.isActive = true " +
            "ORDER BY c.code ASC")
    List<ChartOfAccount> findAllForSelector();

    @Query("SELECT c FROM ChartOfAccount c WHERE c.isActive = true AND c.isHeader = false AND " +
            "(LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))" +
            " ORDER BY c.code ASC")
    List<ChartOfAccount> searchForLookup(@Param("keyword") String keyword, Pageable pageable);
}
