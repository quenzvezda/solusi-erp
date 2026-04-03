package com.solusi.erp.inventory.facility.domain.port;

import com.solusi.erp.core.dto.LookupDto;

/**
 * Port for resolving Facility data as LookupDto.
 * Single source of truth for Facility's autocomplete representation.
 * Consumer slices inject this port instead of querying Facility infrastructure directly.
 */
public interface FacilityLookupProvider {
    LookupDto resolve(Long facilityId);
}
