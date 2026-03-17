package com.solusi.erp.inventory.service;

import com.solusi.erp.inventory.dto.GridRequest;
import com.solusi.erp.inventory.dto.GridResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service for Grid.
 */
public interface GridService {
    Page<GridResponse> findAll(String keyword, Long facilityId, Pageable pageable);
    GridResponse findById(Long id);
    GridRequest getEditData(Long id);
    void create(GridRequest request);
    void update(Long id, GridRequest request);
    void delete(Long id);
}
