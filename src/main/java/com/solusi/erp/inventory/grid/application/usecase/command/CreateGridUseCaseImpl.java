package com.solusi.erp.inventory.grid.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.grid.domain.model.Grid;
import com.solusi.erp.inventory.grid.domain.repository.GridRepository;

public class CreateGridUseCaseImpl implements CreateGridUseCase {

    private final GridRepository repository;

    public CreateGridUseCaseImpl(GridRepository repository) {
        this.repository = repository;
    }

    @Override
    public Grid execute(Long facilityId, String code, String name, String note, Boolean isActive) {
        if (repository.existsByFacilityIdAndCode(facilityId, code)) {
            throw new DomainException("msg.error.grid.duplicate-code");
        }
        Grid grid = Grid.createNew(facilityId, code, name, note, isActive);
        return repository.save(grid);
    }
}
