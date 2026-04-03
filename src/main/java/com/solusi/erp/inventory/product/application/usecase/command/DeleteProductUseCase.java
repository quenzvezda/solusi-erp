package com.solusi.erp.inventory.product.application.usecase.command;

/**
 * Command Use Case to delete a product.
 */
public interface DeleteProductUseCase {
    void execute(Long id);
}
