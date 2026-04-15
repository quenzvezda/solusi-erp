package com.solusi.erp.master.currency.infrastructure.adapter;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import com.solusi.erp.master.currency.infrastructure.persistence.Currency;
import com.solusi.erp.master.currency.infrastructure.persistence.CurrencyJpaRepository;

import java.util.Map;

/**
 * Infrastructure adapter that resolves Currency into LookupDto with consistent subText.
 * SubText format: "{symbol} - {alias}" (e.g. "$ - USD").
 * Symbol is also included in payload for consumers needing only the symbol (e.g. list view).
 * This is the single source of truth — all consumers get the same representation.
 */
public class CurrencyLookupProviderImpl implements CurrencyLookupProvider {

    private final CurrencyJpaRepository currencyJpaRepository;

    public CurrencyLookupProviderImpl(CurrencyJpaRepository currencyJpaRepository) {
        this.currencyJpaRepository = currencyJpaRepository;
    }

    @Override
    public LookupDto resolve(Long currencyId) {
        if (currencyId == null) return null;
        return currencyJpaRepository.findById(currencyId)
                .map(this::toLookupDto)
                .orElse(null);
    }

    private LookupDto toLookupDto(Currency c) {
        return new LookupDto(
                c.getId(),
                c.getName(),
                c.getSymbol() + " - " + c.getAlias(),
                Map.of("symbol", c.getSymbol()));
    }
}
