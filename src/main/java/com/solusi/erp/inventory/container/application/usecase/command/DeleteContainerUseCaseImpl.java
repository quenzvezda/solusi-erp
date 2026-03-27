package com.solusi.erp.inventory.container.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.container.domain.repository.ContainerRepository;

public class DeleteContainerUseCaseImpl implements DeleteContainerUseCase {

    private final ContainerRepository repository;

    public DeleteContainerUseCaseImpl(ContainerRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(Long id) {
        repository.findById(id).orElseThrow(() -> new DomainException("msg.error.container.notfound"));
        repository.delete(id);
    }
}
