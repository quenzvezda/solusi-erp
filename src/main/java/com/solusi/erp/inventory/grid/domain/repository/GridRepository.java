package com.solusi.erp.inventory.grid.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.grid.domain.model.Grid;

import java.util.List;
import java.util.Optional;

public interface GridRepository {
    Grid save(Grid grid);
    Optional<Grid> findById(Long id);
    Page<Grid> findAll(String keyword, Long facilityId, Pageable pageable);
    List<Grid> search(String keyword, Long facilityId, int limit);
    void delete(Long id);
    boolean existsByFacilityIdAndCode(Long facilityId, String code);
    boolean existsByFacilityIdAndCodeAndIdNot(Long facilityId, String code, Long id);
}
