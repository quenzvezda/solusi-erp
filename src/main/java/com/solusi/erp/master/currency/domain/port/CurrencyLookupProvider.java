package com.solusi.erp.master.currency.domain.port;

import com.solusi.erp.core.dto.LookupDto;

/**
 * Port for resolving Currency data as LookupDto.
 * Single source of truth for Currency's autocomplete representation (name + subText).
 * Consumer slices inject this port instead of querying Currency infrastructure directly.
 */
public interface CurrencyLookupProvider {
    LookupDto resolve(Long currencyId);
}
