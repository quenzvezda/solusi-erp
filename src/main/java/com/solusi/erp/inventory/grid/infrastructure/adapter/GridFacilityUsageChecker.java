package com.solusi.erp.inventory.grid.infrastructure.adapter;

import com.solusi.erp.inventory.facility.domain.port.FacilityUsageChecker;
import com.solusi.erp.inventory.grid.infrastructure.persistence.GridJpaRepository;

public class GridFacilityUsageChecker implements FacilityUsageChecker {

    private final GridJpaRepository gridJpaRepository;

    public GridFacilityUsageChecker(GridJpaRepository gridJpaRepository) {
        this.gridJpaRepository = gridJpaRepository;
    }

    @Override
    public boolean isUsed(Long facilityId) {
        return gridJpaRepository.existsByFacilityId(facilityId);
    }
}
