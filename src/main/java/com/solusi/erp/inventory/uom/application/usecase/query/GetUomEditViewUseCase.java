package com.solusi.erp.inventory.uom.application.usecase.query;

import com.solusi.erp.inventory.uom.domain.model.UnitOfMeasure;

import java.util.Optional;

@FunctionalInterface
public interface GetUomEditViewUseCase {
    Optional<UnitOfMeasure> execute(Long id);
}
