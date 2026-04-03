package com.solusi.erp.inventory.grid.domain.port;

import com.solusi.erp.core.dto.LookupDto;

/**
 * Port for resolving Grid data as LookupDto.
 * Single source of truth for Grid's autocomplete representation.
 * Consumer slices inject this port instead of querying Grid infrastructure directly.
 */
public interface GridLookupProvider {
    LookupDto resolve(Long gridId);
}
