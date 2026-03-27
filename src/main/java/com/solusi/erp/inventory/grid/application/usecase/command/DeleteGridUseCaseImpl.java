package com.solusi.erp.inventory.grid.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.grid.domain.repository.GridRepository;

public class DeleteGridUseCaseImpl implements DeleteGridUseCase {

    private final GridRepository repository;

    public DeleteGridUseCaseImpl(GridRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(Long id) {
        repository.findById(id).orElseThrow(() -> new DomainException("msg.error.grid.notfound"));
        repository.delete(id);
    }
}
