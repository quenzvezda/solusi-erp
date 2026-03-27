package com.solusi.erp.inventory.adjustment.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustment;

import java.util.Optional;

/**
 * Domain Repository interface for StockAdjustment aggregate.
 * Pure Java — no Spring, no JPA annotations.
 */
public interface StockAdjustmentRepository {
    Page<StockAdjustment> findAll(String keyword, Pageable pageable);
    Optional<StockAdjustment> findById(Long id);
    StockAdjustment save(StockAdjustment domain);
    void deleteById(Long id);
}
