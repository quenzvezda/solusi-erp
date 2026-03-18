package com.solusi.erp.inventory.service;

import com.solusi.erp.inventory.dto.ProductRequest;
import com.solusi.erp.inventory.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for Product.
 */
public interface ProductService {
    List<ProductResponse> findAll();
    Page<ProductResponse> findAll(String keyword, Pageable pageable);
    ProductResponse findById(Long id);
    ProductRequest getEditData(Long id);
    void create(ProductRequest request);
    void update(Long id, ProductRequest request);
    void delete(Long id);
}
