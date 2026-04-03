package com.solusi.erp.inventory.product.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.product.domain.repository.ProductRepository;

/**
 * Implementation of DeleteProductUseCase.
 * Pure Java.
 */
public class DeleteProductUseCaseImpl implements DeleteProductUseCase {

    private final ProductRepository productRepository;
    private final com.solusi.erp.inventory.product.domain.port.ProductInUseChecker inUseChecker;

    public DeleteProductUseCaseImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
        this.inUseChecker = null;
    }

    // New constructor with in-use checker
    public DeleteProductUseCaseImpl(ProductRepository productRepository, com.solusi.erp.inventory.product.domain.port.ProductInUseChecker inUseChecker) {
        this.productRepository = productRepository;
        this.inUseChecker = inUseChecker;
    }

    @Override
    public void execute(Long id) {
        if (!productRepository.findById(id).isPresent()) {
            throw new DomainException("msg.error.product.not-found");
        }
        if (inUseChecker != null && inUseChecker.isUsed(id)) {
            throw new DomainException("msg.error.product.in-use");
        }
        productRepository.delete(id);
    }
}
