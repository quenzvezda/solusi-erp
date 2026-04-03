package com.solusi.erp.inventory.product.domain.port;

import com.solusi.erp.core.dto.LookupDto;

public interface ProductLookupProvider {
    LookupDto resolve(Long productId);
}
