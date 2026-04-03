package com.solusi.erp.inventory.uom.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.uom.domain.repository.UomRepository;
import com.solusi.erp.inventory.uom.infrastructure.adapter.UomInUseCheckerComposite;

public class DeleteUomUseCaseImpl implements DeleteUomUseCase {

    private final UomRepository repository;
    private final UomInUseCheckerComposite inUseChecker;

    public DeleteUomUseCaseImpl(UomRepository repository, UomInUseCheckerComposite inUseChecker) {
        this.repository = repository;
        this.inUseChecker = inUseChecker;
    }

    @Override
    public void execute(Long id) {
        repository.findById(id).orElseThrow(() -> new DomainException("msg.error.uom.notfound"));
        if (inUseChecker.isUsed(id)) {
            throw new DomainException("msg.error.uom.in-use");
        }
        repository.delete(id);
    }
}
