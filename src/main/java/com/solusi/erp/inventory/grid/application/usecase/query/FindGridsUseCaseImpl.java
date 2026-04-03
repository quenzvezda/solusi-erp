package com.solusi.erp.inventory.grid.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.grid.domain.model.Grid;
import com.solusi.erp.inventory.grid.domain.repository.GridRepository;

public class FindGridsUseCaseImpl implements FindGridsUseCase {

    private final GridRepository repository;

    public FindGridsUseCaseImpl(GridRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Grid> execute(String keyword, Long facilityId, Pageable pageable) {
        return repository.findAll(keyword, facilityId, pageable);
    }
}
