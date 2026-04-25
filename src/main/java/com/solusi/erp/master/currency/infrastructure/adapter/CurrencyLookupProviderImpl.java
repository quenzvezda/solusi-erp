package com.solusi.erp.master.currency.infrastructure.adapter;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import com.solusi.erp.master.currency.infrastructure.persistence.Currency;
import com.solusi.erp.master.currency.infrastructure.persistence.CurrencyJpaRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Infrastructure adapter that resolves Currency into LookupDto with consistent subText.
 * SubText format: "{symbol} - {alias}" (e.g. "$ - USD").
 * Payload includes both symbol and alias for consumers needing direct access without parsing.
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
        Map<String, Object> payload = new HashMap<>();
        payload.put("symbol", c.getSymbol());
        payload.put("alias", c.getAlias());
        return new LookupDto(
                c.getId(),
                c.getName(),
                c.getSymbol() + " - " + c.getAlias(),
                payload);
    }
}
