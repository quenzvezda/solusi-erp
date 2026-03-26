package com.solusi.erp.inventory.brand.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.brand.domain.model.Brand;

import java.util.List;
import java.util.Optional;

/**
 * Domain Repository interface for Brand.
 * Pure Java — no framework dependency.
 */
public interface BrandRepository {
    Brand save(Brand brand);
    Optional<Brand> findById(Long id);
    Page<Brand> findAll(String keyword, Pageable pageable);
    List<Brand> search(String keyword, int limit);
    void delete(Long id);
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, Long id);
}
