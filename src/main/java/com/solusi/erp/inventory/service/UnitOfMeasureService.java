package com.solusi.erp.inventory.service;

import com.solusi.erp.inventory.dto.UnitOfMeasureRequest;
import com.solusi.erp.inventory.dto.UnitOfMeasureResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Unit of Measure.
 */
public interface UnitOfMeasureService {
    Page<UnitOfMeasureResponse> findAll(String keyword, Pageable pageable);
    UnitOfMeasureResponse findById(Long id);
    UnitOfMeasureRequest getEditData(Long id);
    void create(UnitOfMeasureRequest request);
    void update(Long id, UnitOfMeasureRequest request);
    void delete(Long id);
}
