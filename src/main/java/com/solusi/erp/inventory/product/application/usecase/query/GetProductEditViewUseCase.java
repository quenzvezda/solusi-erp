package com.solusi.erp.inventory.product.application.usecase.query;

import com.solusi.erp.inventory.product.domain.model.Product;
import java.util.Optional;

/**
 * Use Case to get product data specifically for edit view.
 */
public interface GetProductEditViewUseCase {
    Optional<Product> execute(Long id);
}
