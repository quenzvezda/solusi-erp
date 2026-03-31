package com.solusi.erp.inventory.uom.infrastructure.adapter;

import com.solusi.erp.inventory.uom.domain.port.UomUsageChecker;

import java.util.List;

/**
 * Aggregates all UomUsageChecker beans registered by consumer slices.
 * Returns true if any checker reports the UoM is in use.
 */
public class UomInUseCheckerComposite {

    private final List<UomUsageChecker> checkers;

    public UomInUseCheckerComposite(List<UomUsageChecker> checkers) {
        this.checkers = checkers;
    }

    public boolean isUsed(Long uomId) {
        return checkers.stream().anyMatch(c -> c.isUsed(uomId));
    }
}
