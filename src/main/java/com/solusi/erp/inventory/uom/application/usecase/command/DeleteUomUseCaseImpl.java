package com.solusi.erp.inventory.uom.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.uom.domain.repository.UomRepository;

public class DeleteUomUseCaseImpl implements DeleteUomUseCase {

    private final UomRepository repository;

    public DeleteUomUseCaseImpl(UomRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(Long id) {
        repository.findById(id).orElseThrow(() -> new DomainException("msg.error.uom.notfound"));
        repository.delete(id);
    }
}
