package com.solusi.erp.inventory.brand.application.usecase.query;

import com.solusi.erp.inventory.brand.domain.model.Brand;
import com.solusi.erp.inventory.brand.domain.repository.BrandRepository;
import java.util.Optional;

public class GetBrandEditViewUseCaseImpl implements GetBrandEditViewUseCase {

    private final BrandRepository repository;

    public GetBrandEditViewUseCaseImpl(BrandRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Brand> execute(Long id) {
        return repository.findById(id);
    }
}
