package com.solusi.erp.inventory.productcategory.domain.port;

public interface ProductCategoryInUseChecker {
    boolean isUsedByAnyProduct(Long categoryId);
}
