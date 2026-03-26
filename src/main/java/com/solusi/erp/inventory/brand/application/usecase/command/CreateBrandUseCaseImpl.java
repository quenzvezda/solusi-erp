package com.solusi.erp.inventory.brand.application.usecase.command;

import com.solusi.erp.core.service.SequenceGeneratorService;
import com.solusi.erp.inventory.brand.domain.model.Brand;
import com.solusi.erp.inventory.brand.domain.repository.BrandRepository;

public class CreateBrandUseCaseImpl implements CreateBrandUseCase {

    private final BrandRepository repository;
    private final SequenceGeneratorService sequenceGeneratorService;

    public CreateBrandUseCaseImpl(BrandRepository repository, SequenceGeneratorService sequenceGeneratorService) {
        this.repository = repository;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public Brand execute(String name, String note) {
        String code = sequenceGeneratorService.generate("BRAND");
        Brand brand = Brand.createNew(code, name, note);
        return repository.save(brand);
    }
}
