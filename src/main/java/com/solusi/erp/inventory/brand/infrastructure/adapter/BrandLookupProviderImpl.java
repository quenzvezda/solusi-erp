package com.solusi.erp.inventory.brand.infrastructure.adapter;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.brand.domain.port.BrandLookupProvider;
import com.solusi.erp.inventory.brand.infrastructure.persistence.BrandJpaRepository;

public class BrandLookupProviderImpl implements BrandLookupProvider {

    private final BrandJpaRepository brandJpaRepository;

    public BrandLookupProviderImpl(BrandJpaRepository brandJpaRepository) {
        this.brandJpaRepository = brandJpaRepository;
    }

    @Override
    public LookupDto resolve(Long brandId) {
        if (brandId == null) return null;
        return brandJpaRepository.findById(brandId)
                .map(e -> new LookupDto(e.getId(), e.getName(), e.getCode()))
                .orElse(null);
    }
}
