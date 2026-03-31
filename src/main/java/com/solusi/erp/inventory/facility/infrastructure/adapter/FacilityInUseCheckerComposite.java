package com.solusi.erp.inventory.facility.infrastructure.adapter;

import com.solusi.erp.inventory.facility.domain.port.FacilityUsageChecker;

import java.util.List;

public class FacilityInUseCheckerComposite {

    private final List<FacilityUsageChecker> checkers;

    public FacilityInUseCheckerComposite(List<FacilityUsageChecker> checkers) {
        this.checkers = checkers;
    }

    public boolean isInUse(Long facilityId) {
        return checkers.stream().anyMatch(c -> c.isUsed(facilityId));
    }
}
