package com.solusi.erp.inventory.stock.domain.repository;

import com.solusi.erp.inventory.stock.domain.model.StockBalance;

import java.util.Optional;

/**
 * Domain Repository interface for StockBalance.
 * Pure Java — no framework dependency.
 */
public interface StockBalanceRepository {

    StockBalance save(StockBalance balance);

    Optional<StockBalance> findByProductContainerSerial(Long productId, Long containerId, String serialNumber);
}
