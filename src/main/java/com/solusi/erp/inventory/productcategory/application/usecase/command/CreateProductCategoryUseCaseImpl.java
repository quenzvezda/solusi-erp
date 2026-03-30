package com.solusi.erp.inventory.productcategory.application.usecase.command;

import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.inventory.productcategory.domain.model.ProductCategoryType;
import com.solusi.erp.inventory.productcategory.domain.model.ProductCategory;
import com.solusi.erp.inventory.productcategory.domain.repository.ProductCategoryRepository;

public class CreateProductCategoryUseCaseImpl implements CreateProductCategoryUseCase {
    private final ProductCategoryRepository repository;
    private final SequenceGeneratorService sequenceGeneratorService;

    public CreateProductCategoryUseCaseImpl(ProductCategoryRepository repository, SequenceGeneratorService sequenceGeneratorService) {
        this.repository = repository;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public ProductCategory execute(String name, ProductCategoryType type, String note) {
        String code = sequenceGeneratorService.generate("PRODUCT_CATEGORY");
        ProductCategory category = ProductCategory.createNew(code, name, type, note);
        return repository.save(category);
    }
}
