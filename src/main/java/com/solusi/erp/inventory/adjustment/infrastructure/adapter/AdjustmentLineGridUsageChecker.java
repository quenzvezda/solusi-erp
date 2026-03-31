package com.solusi.erp.inventory.adjustment.infrastructure.adapter;

import com.solusi.erp.inventory.adjustment.infrastructure.persistence.StockAdjustmentLineJpaRepository;
import com.solusi.erp.inventory.grid.domain.port.GridUsageChecker;

public class AdjustmentLineGridUsageChecker implements GridUsageChecker {

    private final StockAdjustmentLineJpaRepository lineJpaRepository;

    public AdjustmentLineGridUsageChecker(StockAdjustmentLineJpaRepository lineJpaRepository) {
        this.lineJpaRepository = lineJpaRepository;
    }

    @Override
    public boolean isUsed(Long gridId) {
        return lineJpaRepository.existsByGridId(gridId);
    }
}
