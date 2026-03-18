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

    @Query("SELECT s FROM StockAdjustment s WHERE s.code LIKE %:keyword% OR s.note LIKE %:keyword%")
    Page<StockAdjustment> search(@Param("keyword") String keyword, Pageable pageable);
}
