package com.solusi.erp.inventory.productcategory.application.usecase.command;

@FunctionalInterface
public interface DeleteProductCategoryUseCase {
    void execute(Long id);
}
