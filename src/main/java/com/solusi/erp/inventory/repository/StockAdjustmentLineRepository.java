package com.solusi.erp.inventory.repository;

import com.solusi.erp.inventory.model.StockAdjustmentLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Stock Adjustment Lines.
 */
@Repository
public interface StockAdjustmentLineRepository extends JpaRepository<StockAdjustmentLine, Long> {
    
    List<StockAdjustmentLine> findByHeaderId(Long headerId);
}
