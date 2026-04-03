package com.solusi.erp.inventory.productcategory.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.productcategory.domain.model.ProductCategory;
import com.solusi.erp.inventory.productcategory.domain.repository.ProductCategoryRepository;

public class FindProductCategoriesUseCaseImpl implements FindProductCategoriesUseCase {
    private final ProductCategoryRepository repository;

    public FindProductCategoriesUseCaseImpl(ProductCategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<ProductCategory> execute(String keyword, Pageable pageable) {
        return repository.findAll(keyword, pageable);
    }
}
