package com.solusi.erp.inventory.product.application.usecase.query;

import com.solusi.erp.inventory.product.domain.model.Product;
import com.solusi.erp.inventory.product.domain.repository.ProductRepository;
import java.util.Optional;

/**
 * Implementation of GetProductEditViewUseCase.
 * Pure Java.
 */
public class GetProductEditViewUseCaseImpl implements GetProductEditViewUseCase {

    private final ProductRepository productRepository;

    public GetProductEditViewUseCaseImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Optional<Product> execute(Long id) {
        return productRepository.findById(id);
    }
}
