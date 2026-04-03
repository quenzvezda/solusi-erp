package com.solusi.erp.inventory.uom.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.uom.domain.model.UomType;
import com.solusi.erp.inventory.uom.domain.model.UnitOfMeasure;
import com.solusi.erp.inventory.uom.domain.repository.UomRepository;

public class CreateUomUseCaseImpl implements CreateUomUseCase {

    private final UomRepository repository;

    public CreateUomUseCaseImpl(UomRepository repository) {
        this.repository = repository;
    }

    @Override
    public UnitOfMeasure execute(String code, String name, UomType type) {
        if (repository.existsByCode(code)) {
            throw new DomainException("msg.error.uom.duplicate-code");
        }
        UnitOfMeasure uom = UnitOfMeasure.createNew(code, name, type);
        return repository.save(uom);
    }
}
