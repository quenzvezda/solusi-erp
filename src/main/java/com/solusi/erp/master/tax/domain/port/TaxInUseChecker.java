package com.solusi.erp.master.tax.domain.port;

/**
 * Port for checking if a Tax is currently referenced by other entities.
 * Part of the Smart Delete pattern — pure Java, no framework dependencies.
 */
public interface TaxInUseChecker {
    boolean isInUse(Long taxId);
}
