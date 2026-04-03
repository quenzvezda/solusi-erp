package com.solusi.erp.inventory.product.domain.port;

/**
 * SPI: Implementations should check whether a product is referenced in other slices (e.g., stock adjustment lines).
 */
public interface ProductInUseChecker {
    boolean isUsed(Long productId);
}
