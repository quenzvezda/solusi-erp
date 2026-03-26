package com.solusi.erp.inventory.productcategory.application.usecase.query;

import com.solusi.erp.inventory.productcategory.domain.model.ProductCategory;
import java.util.Optional;

@FunctionalInterface
public interface GetProductCategoryEditViewUseCase {
    Optional<ProductCategory> execute(Long id);
}
