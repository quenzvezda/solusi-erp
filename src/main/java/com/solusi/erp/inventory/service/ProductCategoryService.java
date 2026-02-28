package com.solusi.erp.inventory.service;

import com.solusi.erp.inventory.dto.ProductCategoryRequest;
import com.solusi.erp.inventory.dto.ProductCategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Product Category.
 */
public interface ProductCategoryService {
    Page<ProductCategoryResponse> findAll(String keyword, Pageable pageable);
    ProductCategoryResponse findById(Long id);
    ProductCategoryRequest getEditData(Long id);
    void create(ProductCategoryRequest request);
    void update(Long id, ProductCategoryRequest request);
    void delete(Long id);
}
