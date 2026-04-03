package com.solusi.erp.inventory.adjustment.infrastructure.adapter;

import com.solusi.erp.inventory.adjustment.infrastructure.persistence.StockAdjustmentLineJpaRepository;
import com.solusi.erp.inventory.uom.domain.port.UomUsageChecker;

/**
 * Checks whether any StockAdjustmentLine references the given UoM.
 */
public class AdjustmentLineUomUsageChecker implements UomUsageChecker {

    private final StockAdjustmentLineJpaRepository lineRepository;

    public AdjustmentLineUomUsageChecker(StockAdjustmentLineJpaRepository lineRepository) {
        this.lineRepository = lineRepository;
    }

    @Override
    public boolean isUsed(Long uomId) {
        return lineRepository.existsByUomId(uomId);
    }
}
