package com.solusi.erp.inventory.facility.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.facility.domain.model.Facility;

@FunctionalInterface
public interface FindFacilitiesUseCase {
    Page<Facility> execute(String keyword, Pageable pageable);
}
