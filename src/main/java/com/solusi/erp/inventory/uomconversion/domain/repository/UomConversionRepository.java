package com.solusi.erp.inventory.uomconversion.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.uomconversion.domain.model.UomConversion;

import java.util.List;
import java.util.Optional;

/**
 * Domain Repository interface for UomConversion.
 * Pure Java — no framework dependency.
 */
public interface UomConversionRepository {
    Page<UomConversion> findAll(String keyword, Pageable pageable);
    Optional<UomConversion> findById(Long id);
    UomConversion save(UomConversion domain);
    void deleteById(Long id);
    boolean existsById(Long id);
    boolean existsByProductIdAndFromUomId(Long productId, Long fromUomId);
    boolean existsByProductIdAndFromUomIdAndIdNot(Long productId, Long fromUomId, Long id);
    List<UomConversion> findByProductId(Long productId);
}
