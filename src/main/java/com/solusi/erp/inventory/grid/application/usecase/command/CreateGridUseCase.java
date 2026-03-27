package com.solusi.erp.inventory.grid.application.usecase.command;

import com.solusi.erp.inventory.grid.domain.model.Grid;

@FunctionalInterface
public interface CreateGridUseCase {
    Grid execute(Long facilityId, String code, String name, String note, Boolean isActive);
}
