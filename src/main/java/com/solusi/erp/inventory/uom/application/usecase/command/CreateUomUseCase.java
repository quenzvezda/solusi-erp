package com.solusi.erp.inventory.uom.application.usecase.command;

import com.solusi.erp.inventory.uom.domain.model.UomType;
import com.solusi.erp.inventory.uom.domain.model.UnitOfMeasure;

@FunctionalInterface
public interface CreateUomUseCase {
    UnitOfMeasure execute(String code, String name, UomType type);
}
