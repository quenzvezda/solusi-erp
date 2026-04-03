package com.solusi.erp.inventory.brand.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.brand.domain.model.Brand;
import com.solusi.erp.inventory.brand.domain.repository.BrandRepository;

public class FindBrandsUseCaseImpl implements FindBrandsUseCase {

    private final BrandRepository repository;

    public FindBrandsUseCaseImpl(BrandRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Brand> execute(String keyword, Pageable pageable) {
        return repository.findAll(keyword, pageable);
    }
}
