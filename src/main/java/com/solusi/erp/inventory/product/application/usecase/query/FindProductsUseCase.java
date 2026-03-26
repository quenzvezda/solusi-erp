package com.solusi.erp.inventory.product.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.product.domain.model.Product;

/**
 * Use Case to list products with optional search and pagination.
 */
public interface FindProductsUseCase {
    Page<Product> execute(String keyword, Pageable pageable);
}
