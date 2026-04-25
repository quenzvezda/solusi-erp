package com.solusi.erp.inventory.uom.infrastructure.adapter;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.uom.domain.port.UomLookupProvider;
import com.solusi.erp.inventory.uom.infrastructure.persistence.UomJpaRepository;

/**
 * Infrastructure adapter that resolves UnitOfMeasure into LookupDto with consistent subText.
 * SubText format: "{code}" (e.g. "KG", "PCS").
 * This is the single source of truth — all consumers get the same representation.
 */
public class UomLookupProviderImpl implements UomLookupProvider {

    private final UomJpaRepository uomJpaRepository;

    public UomLookupProviderImpl(UomJpaRepository uomJpaRepository) {
        this.uomJpaRepository = uomJpaRepository;
    }

    @Override
    public LookupDto resolve(Long uomId) {
        if (uomId == null) return null;
        return uomJpaRepository.findById(uomId)
                .map(e -> new LookupDto(e.getId(), e.getName(), e.getCode()))
                .orElse(null);
    }
}
