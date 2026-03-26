package com.solusi.erp.inventory.productcategory.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.productcategory.domain.model.ProductCategory;

@FunctionalInterface
public interface FindProductCategoriesUseCase {
    Page<ProductCategory> execute(String keyword, Pageable pageable);
}
