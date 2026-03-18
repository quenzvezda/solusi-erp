package com.solusi.erp.inventory.service;

import com.solusi.erp.inventory.dto.StockMovementPayload;

/**
 * Service for managing Stock Balances and Movements.
 */
public interface StockService {

    /**
     * Adjust stock based on the provided payload.
     * Updates StockBalance and logs InventoryMovement atomically.
     * 
     * @param payload The adjustment details.
     */
    void adjust(StockMovementPayload payload);
}
