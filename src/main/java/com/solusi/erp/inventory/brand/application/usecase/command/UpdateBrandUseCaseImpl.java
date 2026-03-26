package com.solusi.erp.inventory.brand.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.brand.domain.model.Brand;
import com.solusi.erp.inventory.brand.domain.repository.BrandRepository;

public class UpdateBrandUseCaseImpl implements UpdateBrandUseCase {

    private final BrandRepository repository;

    public UpdateBrandUseCaseImpl(BrandRepository repository) {
        this.repository = repository;
    }

    @Override
    public Brand execute(Long id, String name, String note) {
        Brand brand = repository.findById(id)
            .orElseThrow(() -> new DomainException("msg.error.brand.notfound"));
        return repository.save(new Brand(brand.getMetadata(), brand.getCode(), name, note));
    }
}
