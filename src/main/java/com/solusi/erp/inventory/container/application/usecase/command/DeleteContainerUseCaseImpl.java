package com.solusi.erp.inventory.container.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.container.domain.repository.ContainerRepository;
import com.solusi.erp.inventory.container.infrastructure.adapter.ContainerInUseCheckerComposite;

public class DeleteContainerUseCaseImpl implements DeleteContainerUseCase {

    private final ContainerRepository repository;
    private final ContainerInUseCheckerComposite inUseChecker;

    public DeleteContainerUseCaseImpl(ContainerRepository repository,
                                      ContainerInUseCheckerComposite inUseChecker) {
        this.repository = repository;
        this.inUseChecker = inUseChecker;
    }

    @Override
    public void execute(Long id) {
        repository.findById(id).orElseThrow(() -> new DomainException("msg.error.container.notfound"));
        if (inUseChecker.isUsed(id)) {
            throw new DomainException("msg.error.container.in-use");
        }
        repository.delete(id);
    }
}
