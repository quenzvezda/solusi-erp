package com.solusi.erp.inventory.stock.infrastructure.adapter;

import com.solusi.erp.inventory.container.domain.port.ContainerUsageChecker;
import com.solusi.erp.inventory.stock.infrastructure.persistence.StockBalanceJpaRepository;

/**
 * Checks whether any StockBalance record references the given container.
 * If stock data exists for a container, the container must not be deleted.
 */
public class StockBalanceContainerUsageChecker implements ContainerUsageChecker {

    private final StockBalanceJpaRepository stockBalanceRepository;

    public StockBalanceContainerUsageChecker(StockBalanceJpaRepository stockBalanceRepository) {
        this.stockBalanceRepository = stockBalanceRepository;
    }

    @Override
    public boolean isUsed(Long containerId) {
        return stockBalanceRepository.existsByContainerId(containerId);
    }
}
