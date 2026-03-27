package com.solusi.erp.inventory.container.application.usecase.query;

import com.solusi.erp.inventory.container.domain.model.Container;
import com.solusi.erp.inventory.container.domain.repository.ContainerRepository;
import java.util.Optional;

public class GetContainerEditViewUseCaseImpl implements GetContainerEditViewUseCase {

    private final ContainerRepository repository;

    public GetContainerEditViewUseCaseImpl(ContainerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Container> execute(Long id) {
        return repository.findById(id);
    }
}
