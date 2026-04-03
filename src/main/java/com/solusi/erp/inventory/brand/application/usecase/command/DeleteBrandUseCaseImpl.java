package com.solusi.erp.inventory.brand.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.brand.domain.port.BrandInUseChecker;
import com.solusi.erp.inventory.brand.domain.repository.BrandRepository;

public class DeleteBrandUseCaseImpl implements DeleteBrandUseCase {

    private final BrandRepository repository;
    private final BrandInUseChecker inUseChecker;

    public DeleteBrandUseCaseImpl(BrandRepository repository, BrandInUseChecker inUseChecker) {
        this.repository = repository;
        this.inUseChecker = inUseChecker;
    }

    @Override
    public void execute(Long id) {
        repository.findById(id).orElseThrow(() -> new DomainException("msg.error.brand.notfound"));
        if (inUseChecker.isUsedByAnyProduct(id)) {
            throw new DomainException("msg.error.brand.in-use");
        }
        repository.delete(id);
    }
}
