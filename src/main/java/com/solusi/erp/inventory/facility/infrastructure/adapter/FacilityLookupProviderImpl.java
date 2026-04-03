package com.solusi.erp.inventory.facility.infrastructure.adapter;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.facility.domain.port.FacilityLookupProvider;
import com.solusi.erp.inventory.facility.infrastructure.persistence.FacilityEntity;
import com.solusi.erp.inventory.facility.infrastructure.persistence.FacilityJpaRepository;

/**
 * Infrastructure adapter that resolves Facility into LookupDto with consistent subText.
 * SubText format: "{code}" — consistent with FacilityLookupController response.
 * This is the single source of truth for Facility autocomplete pre-edit representation.
 */
public class FacilityLookupProviderImpl implements FacilityLookupProvider {

    private final FacilityJpaRepository facilityJpaRepository;

    public FacilityLookupProviderImpl(FacilityJpaRepository facilityJpaRepository) {
        this.facilityJpaRepository = facilityJpaRepository;
    }

    @Override
    public LookupDto resolve(Long facilityId) {
        if (facilityId == null) return null;
        return facilityJpaRepository.findById(facilityId)
                .map(this::toLookupDto)
                .orElse(null);
    }

    private LookupDto toLookupDto(FacilityEntity entity) {
        return new LookupDto(entity.getId(), entity.getName(), entity.getCode());
    }
}
