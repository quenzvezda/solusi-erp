package com.solusi.erp.master.tax.infrastructure.adapter;

import com.solusi.erp.master.tax.domain.port.TaxInUseChecker;

/**
 * Infrastructure adapter for TaxInUseChecker.
 * Currently no known consumers reference Tax directly,
 * but this placeholder allows future modules to register checks.
 */
public class TaxInUseCheckerImpl implements TaxInUseChecker {

    public TaxInUseCheckerImpl() {
    }

    @Override
    public boolean isInUse(Long taxId) {
        // Placeholder: no known FK references to taxes table yet.
        // When a module starts referencing Tax (e.g., Invoice line items),
        // inject its JPA repository here and check existsByTaxId(taxId).
        return false;
    }
}
