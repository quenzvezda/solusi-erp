package com.solusi.erp.inventory.product.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.product.domain.repository.ProductRepository;

/**
 * Implementation of DeleteProductUseCase.
 * Pure Java.
 */
public class DeleteProductUseCaseImpl implements DeleteProductUseCase {

    private final ProductRepository productRepository;

    public DeleteProductUseCaseImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void execute(Long id) {
        if (!productRepository.findById(id).isPresent()) {
            throw new DomainException("msg.error.product.not-found");
        }
        productRepository.delete(id);
    }
}
