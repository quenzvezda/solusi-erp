package com.solusi.erp.inventory.uom.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.uom.domain.model.UnitOfMeasure;
import com.solusi.erp.inventory.uom.domain.repository.UomRepository;

public class FindUomsUseCaseImpl implements FindUomsUseCase {

    private final UomRepository repository;

    public FindUomsUseCaseImpl(UomRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<UnitOfMeasure> execute(String keyword, Pageable pageable) {
        return repository.findAll(keyword, pageable);
    }
}
