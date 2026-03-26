package com.solusi.erp.inventory.product.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.product.domain.model.Product;
import com.solusi.erp.inventory.product.domain.repository.ProductRepository;

/**
 * Implementation of FindProductsUseCase.
 * Pure Java.
 */
public class FindProductsUseCaseImpl implements FindProductsUseCase {

    private final ProductRepository productRepository;

    public FindProductsUseCaseImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Page<Product> execute(String keyword, Pageable pageable) {
        return productRepository.findAll(keyword, pageable);
    }
}
