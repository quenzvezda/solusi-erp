package com.solusi.erp.inventory.productcategory.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.productcategory.domain.port.ProductCategoryInUseChecker;
import com.solusi.erp.inventory.productcategory.domain.repository.ProductCategoryRepository;

public class DeleteProductCategoryUseCaseImpl implements DeleteProductCategoryUseCase {
    private final ProductCategoryRepository repository;
    private final ProductCategoryInUseChecker inUseChecker;

    public DeleteProductCategoryUseCaseImpl(ProductCategoryRepository repository,
                                            ProductCategoryInUseChecker inUseChecker) {
        this.repository = repository;
        this.inUseChecker = inUseChecker;
    }

    @Override
    public void execute(Long id) {
        repository.findById(id)
            .orElseThrow(() -> new DomainException("msg.error.product-category.notfound"));
        if (inUseChecker.isUsedByAnyProduct(id)) {
            throw new DomainException("msg.error.product-category.in-use");
        }
        repository.delete(id);
    }
}
