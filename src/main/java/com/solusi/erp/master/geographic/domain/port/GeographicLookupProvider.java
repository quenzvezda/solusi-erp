package com.solusi.erp.master.geographic.domain.port;

import com.solusi.erp.core.dto.LookupDto;

/**
 * Port for resolving Geographic data as LookupDto.
 * Single source of truth for Geographic's autocomplete representation.
 * Consumer slices inject this port instead of querying Geographic infrastructure directly.
 */
public interface GeographicLookupProvider {
    LookupDto resolve(Long geographicId);
}
