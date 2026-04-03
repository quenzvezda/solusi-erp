package com.solusi.erp.inventory.productcategory.application.usecase.query;

import com.solusi.erp.inventory.productcategory.domain.model.ProductCategory;
import com.solusi.erp.inventory.productcategory.domain.repository.ProductCategoryRepository;
import java.util.Optional;

public class GetProductCategoryEditViewUseCaseImpl implements GetProductCategoryEditViewUseCase {
    private final ProductCategoryRepository repository;

    public GetProductCategoryEditViewUseCaseImpl(ProductCategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ProductCategory> execute(Long id) {
        return repository.findById(id);
    }
}
