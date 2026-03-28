package com.solusi.erp.master.geographic.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.geographic.domain.repository.GeographicRepository;

public class DeleteGeographicUseCaseImpl implements DeleteGeographicUseCase {

    private final GeographicRepository repository;

    public DeleteGeographicUseCaseImpl(GeographicRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(Long id) {
        repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.geographic.notfound"));
        repository.delete(id);
    }
}
