package com.solusi.erp.inventory.uom.application.usecase.command;

import com.solusi.erp.inventory.model.UomType;
import com.solusi.erp.inventory.uom.domain.model.UnitOfMeasure;

@FunctionalInterface
public interface UpdateUomUseCase {
    UnitOfMeasure execute(Long id, String name, UomType type);
}
