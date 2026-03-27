package com.solusi.erp.inventory.grid.application.usecase.command;

import com.solusi.erp.inventory.grid.domain.model.Grid;

@FunctionalInterface
public interface UpdateGridUseCase {
    Grid execute(Long id, String name, String note, Boolean isActive);
}
