package com.solusi.erp.inventory.stock.domain.repository;

import com.solusi.erp.inventory.stock.domain.model.ValuationLayer;

import java.math.BigDecimal;
import java.util.List;

/**
 * Domain Repository interface for ValuationLayer.
 * Pure Java — no framework dependency.
 */
public interface ValuationLayerRepository {

    void save(ValuationLayer layer);

    /**
     * Find layers with remaining quantity, ordered by creation date (oldest first) for FIFO.
     */
    List<ValuationLayer> findAvailableLayers(Long productId, Long containerId, BigDecimal minQuantity);

    /**
     * Find layers for serialized items with remaining quantity.
     */
    List<ValuationLayer> findAvailableLayersBySerial(Long productId, Long containerId,
                                                     String serialNumber, BigDecimal minQuantity);
}
