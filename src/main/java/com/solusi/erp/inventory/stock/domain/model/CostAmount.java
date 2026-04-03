package com.solusi.erp.inventory.stock.domain.model;

import java.math.BigDecimal;

/**
 * Value Object representing a monetary amount with multi-currency support.
 * Pure Java — no Spring, no Lombok, no JPA.
 */
public record CostAmount(
    Long currencyId,
    BigDecimal exchangeRate,
    BigDecimal originalAmount,
    BigDecimal localAmount
) {

    /**
     * Factory method that auto-calculates localAmount.
     */
    public static CostAmount of(Long currencyId, BigDecimal exchangeRate, BigDecimal originalAmount) {
        BigDecimal rate = exchangeRate != null ? exchangeRate : BigDecimal.ONE;
        BigDecimal original = originalAmount != null ? originalAmount : BigDecimal.ZERO;
        BigDecimal local = original.multiply(rate);
        return new CostAmount(currencyId, rate, original, local);
    }

    /**
     * Create a CostAmount with only local amount (used for FIFO weighted average result).
     */
    public static CostAmount localOnly(BigDecimal localAmount) {
        return new CostAmount(null, null, null, localAmount);
    }
}
