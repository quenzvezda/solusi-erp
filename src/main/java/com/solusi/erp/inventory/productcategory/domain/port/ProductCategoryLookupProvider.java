package com.solusi.erp.inventory.productcategory.domain.port;

import com.solusi.erp.core.dto.LookupDto;

public interface ProductCategoryLookupProvider {
    LookupDto resolve(Long categoryId);
}
