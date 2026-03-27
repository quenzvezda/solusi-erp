package com.solusi.erp.inventory.uom.application.usecase.query;

import com.solusi.erp.inventory.uom.domain.model.UnitOfMeasure;
import com.solusi.erp.inventory.uom.domain.repository.UomRepository;

import java.util.Optional;

public class GetUomEditViewUseCaseImpl implements GetUomEditViewUseCase {

    private final UomRepository repository;

    public GetUomEditViewUseCaseImpl(UomRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<UnitOfMeasure> execute(Long id) {
        return repository.findById(id);
    }
}
