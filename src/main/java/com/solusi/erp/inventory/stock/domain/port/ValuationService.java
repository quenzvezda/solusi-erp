package com.solusi.erp.inventory.stock.domain.port;

import com.solusi.erp.core.model.CurrencyAmount;

import java.math.BigDecimal;

public interface ValuationService {
    /**
     * Create a new valuation layer for inbound transactions.
     */
    void addStock(Long productId, Long containerId, String serialNumber,
                  BigDecimal quantity, CurrencyAmount unitCost);

    /**
     * Consume existing layers for outbound transactions (FIFO).
     * Returns the weighted average unit cost in local currency for the consumed quantity.
     */
    CurrencyAmount consumeStock(Long productId, Long containerId, String serialNumber, BigDecimal quantity);
}
