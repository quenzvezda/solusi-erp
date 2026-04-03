package com.solusi.erp.inventory.product.application.usecase.query;

import com.solusi.erp.inventory.product.domain.model.Product;
import java.util.Optional;

/**
 * Use Case to get a single product by ID.
 */
public interface GetProductUseCase {
    Optional<Product> execute(Long id);
}
