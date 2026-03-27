package com.solusi.erp.inventory.grid.application.usecase.query;

import com.solusi.erp.inventory.grid.domain.model.Grid;
import java.util.Optional;

@FunctionalInterface
public interface GetGridEditViewUseCase {
    Optional<Grid> execute(Long id);
}
