package com.solusi.erp.inventory.service;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.dto.GridRequest;
import com.solusi.erp.inventory.dto.GridResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service for Grid.
 */
public interface GridService {
    LookupDto getLookupGrid(Long id);
    List<LookupDto> lookupGrids(String keyword, Long facilityId, int limit);
    Page<GridResponse> findAll(String keyword, Long facilityId, Pageable pageable);
    GridResponse findById(Long id);
    GridRequest getEditData(Long id);
    void create(GridRequest request);
    void update(Long id, GridRequest request);
    void delete(Long id);
}
