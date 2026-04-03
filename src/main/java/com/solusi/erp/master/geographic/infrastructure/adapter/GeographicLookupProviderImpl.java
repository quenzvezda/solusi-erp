package com.solusi.erp.master.geographic.infrastructure.adapter;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.geographic.domain.port.GeographicLookupProvider;
import com.solusi.erp.master.geographic.infrastructure.persistence.Geographic;
import com.solusi.erp.master.geographic.infrastructure.persistence.GeographicJpaRepository;

/**
 * Infrastructure adapter that resolves Geographic into LookupDto with consistent subText.
 * SubText format: "{code}" — consistent with GeographicLookupController response.
 * This is the single source of truth for Geographic autocomplete pre-edit representation.
 */
public class GeographicLookupProviderImpl implements GeographicLookupProvider {

    private final GeographicJpaRepository geographicJpaRepository;

    public GeographicLookupProviderImpl(GeographicJpaRepository geographicJpaRepository) {
        this.geographicJpaRepository = geographicJpaRepository;
    }

    @Override
    public LookupDto resolve(Long geographicId) {
        if (geographicId == null) return null;
        return geographicJpaRepository.findById(geographicId)
                .map(this::toLookupDto)
                .orElse(null);
    }

    private LookupDto toLookupDto(Geographic geo) {
        return new LookupDto(geo.getId(), geo.getName(), geo.getCode());
    }
}
