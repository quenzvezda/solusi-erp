package com.solusi.erp.inventory.uom.domain.port;

import com.solusi.erp.core.dto.LookupDto;

/**
 * Port for resolving UnitOfMeasure data as LookupDto.
 * Single source of truth for UoM's autocomplete representation (name + subText).
 * Consumer slices inject this port instead of querying UoM infrastructure directly.
 */
public interface UomLookupProvider {
    LookupDto resolve(Long uomId);
}
