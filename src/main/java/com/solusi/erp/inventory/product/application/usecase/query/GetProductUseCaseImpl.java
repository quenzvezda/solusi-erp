package com.solusi.erp.inventory.product.application.usecase.query;

import com.solusi.erp.inventory.product.domain.model.Product;
import com.solusi.erp.inventory.product.domain.repository.ProductRepository;
import java.util.Optional;

/**
 * Implementation of GetProductUseCase.
 * Pure Java.
 */
public class GetProductUseCaseImpl implements GetProductUseCase {

    private final ProductRepository productRepository;

    public GetProductUseCaseImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Optional<Product> execute(Long id) {
        return productRepository.findById(id);
    }
}
