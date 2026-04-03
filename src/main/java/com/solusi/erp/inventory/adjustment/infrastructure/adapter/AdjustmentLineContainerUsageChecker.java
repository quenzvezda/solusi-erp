package com.solusi.erp.inventory.adjustment.infrastructure.adapter;

import com.solusi.erp.inventory.adjustment.infrastructure.persistence.StockAdjustmentLineJpaRepository;
import com.solusi.erp.inventory.container.domain.port.ContainerUsageChecker;

/**
 * Checks whether any StockAdjustmentLine references the given container.
 */
public class AdjustmentLineContainerUsageChecker implements ContainerUsageChecker {

    private final StockAdjustmentLineJpaRepository lineRepository;

    public AdjustmentLineContainerUsageChecker(StockAdjustmentLineJpaRepository lineRepository) {
        this.lineRepository = lineRepository;
    }

    @Override
    public boolean isUsed(Long containerId) {
        return lineRepository.existsByContainerId(containerId);
    }
}
