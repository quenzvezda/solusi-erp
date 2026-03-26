package com.solusi.erp.inventory.productcategory.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.model.ProductCategoryType;
import com.solusi.erp.inventory.productcategory.domain.model.ProductCategory;
import com.solusi.erp.inventory.productcategory.domain.repository.ProductCategoryRepository;

public class UpdateProductCategoryUseCaseImpl implements UpdateProductCategoryUseCase {
    private final ProductCategoryRepository repository;

    public UpdateProductCategoryUseCaseImpl(ProductCategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public ProductCategory execute(Long id, String name, ProductCategoryType type, String note) {
        ProductCategory category = repository.findById(id)
            .orElseThrow(() -> new DomainException("msg.error.product-category.notfound"));
        category.update(name, type, note);
        return repository.save(new ProductCategory(
            category.getMetadata(), category.getCode(), name, type, note
        ));
    }
}
