package com.solusi.erp.inventory.container.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.container.domain.model.Container;
import com.solusi.erp.inventory.container.domain.repository.ContainerRepository;

public class FindContainersUseCaseImpl implements FindContainersUseCase {

    private final ContainerRepository repository;

    public FindContainersUseCaseImpl(ContainerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Container> execute(String keyword, Long gridId, Pageable pageable) {
        return repository.findAll(keyword, gridId, pageable);
    }
}
