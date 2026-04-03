package com.solusi.erp.master.tax.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.master.tax.domain.model.Tax;

import java.util.Optional;

/**
 * Domain Repository interface for Tax.
 * Pure Java — no framework dependency.
 */
public interface TaxRepository {
    Tax save(Tax tax);
    Optional<Tax> findById(Long id);
    Page<Tax> findAll(String keyword, Pageable pageable);
    void delete(Long id);
    boolean existsByCode(String code);
}
