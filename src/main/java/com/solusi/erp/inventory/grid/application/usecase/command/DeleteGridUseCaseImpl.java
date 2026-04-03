package com.solusi.erp.inventory.grid.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.grid.domain.repository.GridRepository;
import com.solusi.erp.inventory.grid.infrastructure.adapter.GridInUseCheckerComposite;

public class DeleteGridUseCaseImpl implements DeleteGridUseCase {

    private final GridRepository repository;
    private final GridInUseCheckerComposite inUseChecker;

    public DeleteGridUseCaseImpl(GridRepository repository,
                                 GridInUseCheckerComposite inUseChecker) {
        this.repository = repository;
        this.inUseChecker = inUseChecker;
    }

    @Override
    public void execute(Long id) {
        repository.findById(id).orElseThrow(() -> new DomainException("msg.error.grid.notfound"));
        if (inUseChecker.isInUse(id)) {
            throw new DomainException("msg.error.grid.in-use");
        }
        repository.delete(id);
    }
}
