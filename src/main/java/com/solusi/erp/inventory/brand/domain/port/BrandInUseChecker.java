package com.solusi.erp.inventory.brand.domain.port;

public interface BrandInUseChecker {
    boolean isUsedByAnyProduct(Long brandId);
}
