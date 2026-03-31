package com.solusi.erp.inventory.adjustment.infrastructure.adapter;

import com.solusi.erp.inventory.adjustment.infrastructure.persistence.StockAdjustmentJpaRepository;
import com.solusi.erp.inventory.facility.domain.port.FacilityUsageChecker;

public class AdjustmentFacilityUsageChecker implements FacilityUsageChecker {

    private final StockAdjustmentJpaRepository adjustmentJpaRepository;

    public AdjustmentFacilityUsageChecker(StockAdjustmentJpaRepository adjustmentJpaRepository) {
        this.adjustmentJpaRepository = adjustmentJpaRepository;
    }

    @Override
    public boolean isUsed(Long facilityId) {
        return adjustmentJpaRepository.existsByFacilityId(facilityId);
    }
}
