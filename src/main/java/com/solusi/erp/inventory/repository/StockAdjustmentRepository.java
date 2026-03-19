package com.solusi.erp.inventory.repository;

import com.solusi.erp.inventory.model.StockAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * Repository for Stock Adjustment Header.
 */
@Repository
public interface StockAdjustmentRepository extends JpaRepository<StockAdjustment, Long> {

    @Query("SELECT s FROM StockAdjustment s WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.note) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<StockAdjustment> search(@Param("keyword") String keyword, Pageable pageable);
}
