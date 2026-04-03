package com.solusi.erp.inventory.product.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.product.domain.model.Product;
import java.util.Optional;

/**
 * Domain Repository Interface for Product.
 */
public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(Long id);
    Optional<Product> findByCode(String code);
    Page<Product> findAll(String keyword, Pageable pageable);
    void delete(Long id);
    boolean existsByCode(String code);
}
