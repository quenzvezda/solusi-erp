package com.solusi.erp.inventory.brand.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.brand.domain.repository.BrandRepository;

public class DeleteBrandUseCaseImpl implements DeleteBrandUseCase {

    private final BrandRepository repository;

    public DeleteBrandUseCaseImpl(BrandRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(Long id) {
        repository.findById(id).orElseThrow(() -> new DomainException("msg.error.brand.notfound"));
        repository.delete(id);
    }
}
