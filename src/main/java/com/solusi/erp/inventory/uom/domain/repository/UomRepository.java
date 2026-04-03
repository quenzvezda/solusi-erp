package com.solusi.erp.inventory.uom.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.uom.domain.model.UomType;
import com.solusi.erp.inventory.uom.domain.model.UnitOfMeasure;

import java.util.List;
import java.util.Optional;

/**
 * Domain Repository interface for UnitOfMeasure.
 * Pure Java — no framework dependency.
 */
public interface UomRepository {
    UnitOfMeasure save(UnitOfMeasure uom);
    Optional<UnitOfMeasure> findById(Long id);
    Page<UnitOfMeasure> findAll(String keyword, Pageable pageable);
    List<UnitOfMeasure> search(String keyword, int limit);
    void delete(Long id);
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, Long id);
    List<UnitOfMeasure> findByType(UomType type);
}
