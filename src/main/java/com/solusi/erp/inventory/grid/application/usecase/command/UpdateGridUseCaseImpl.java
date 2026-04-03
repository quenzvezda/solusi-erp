package com.solusi.erp.inventory.grid.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.grid.domain.model.Grid;
import com.solusi.erp.inventory.grid.domain.repository.GridRepository;

public class UpdateGridUseCaseImpl implements UpdateGridUseCase {

    private final GridRepository repository;

    public UpdateGridUseCaseImpl(GridRepository repository) {
        this.repository = repository;
    }

    @Override
    public Grid execute(Long id, String name, String note, Boolean isActive) {
        Grid grid = repository.findById(id)
            .orElseThrow(() -> new DomainException("msg.error.grid.notfound"));
        grid.update(name, note, isActive);
        return repository.save(grid);
    }
}
