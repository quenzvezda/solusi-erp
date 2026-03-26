package com.solusi.erp.inventory.productcategory.application.usecase.command;

import com.solusi.erp.inventory.model.ProductCategoryType;
import com.solusi.erp.inventory.productcategory.domain.model.ProductCategory;

@FunctionalInterface
public interface UpdateProductCategoryUseCase {
    ProductCategory execute(Long id, String name, ProductCategoryType type, String note);
}
