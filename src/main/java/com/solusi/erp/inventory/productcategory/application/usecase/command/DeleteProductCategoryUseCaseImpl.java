package com.solusi.erp.inventory.productcategory.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.productcategory.domain.repository.ProductCategoryRepository;

public class DeleteProductCategoryUseCaseImpl implements DeleteProductCategoryUseCase {
    private final ProductCategoryRepository repository;

    public DeleteProductCategoryUseCaseImpl(ProductCategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(Long id) {
        if (!repository.existsByCode(String.valueOf(id))) {
            repository.findById(id).orElseThrow(() -> new DomainException("msg.error.product-category.notfound"));
        }
        repository.delete(id);
    }
}
