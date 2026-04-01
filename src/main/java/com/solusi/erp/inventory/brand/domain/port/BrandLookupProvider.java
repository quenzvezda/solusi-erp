package com.solusi.erp.inventory.brand.domain.port;

import com.solusi.erp.core.dto.LookupDto;

public interface BrandLookupProvider {
    LookupDto resolve(Long brandId);
}
