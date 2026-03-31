package com.solusi.erp.master.party.domain.port;

import com.solusi.erp.core.dto.LookupDto;

/**
 * Port for resolving Party data as LookupDto.
 * Single source of truth for Party's autocomplete representation (name + subText).
 * Consumer slices inject this port instead of querying Party infrastructure directly.
 */
public interface PartyLookupProvider {
    LookupDto resolve(Long partyId);
}
