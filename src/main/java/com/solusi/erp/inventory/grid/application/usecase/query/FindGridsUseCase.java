package com.solusi.erp.inventory.grid.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.grid.domain.model.Grid;

@FunctionalInterface
public interface FindGridsUseCase {
    Page<Grid> execute(String keyword, Long facilityId, Pageable pageable);
}
