package com.solusi.erp.inventory.grid.application.usecase.query;

import com.solusi.erp.inventory.grid.domain.model.Grid;
import com.solusi.erp.inventory.grid.domain.repository.GridRepository;
import java.util.Optional;

public class GetGridEditViewUseCaseImpl implements GetGridEditViewUseCase {

    private final GridRepository repository;

    public GetGridEditViewUseCaseImpl(GridRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Grid> execute(Long id) {
        return repository.findById(id);
    }
}
