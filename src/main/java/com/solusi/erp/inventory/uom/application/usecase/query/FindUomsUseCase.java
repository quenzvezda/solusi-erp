package com.solusi.erp.inventory.uom.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.uom.domain.model.UnitOfMeasure;

@FunctionalInterface
public interface FindUomsUseCase {
    Page<UnitOfMeasure> execute(String keyword, Pageable pageable);
}
