package com.solusi.erp.inventory.uom.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.model.UomType;
import com.solusi.erp.inventory.uom.domain.model.UnitOfMeasure;
import com.solusi.erp.inventory.uom.domain.repository.UomRepository;

public class UpdateUomUseCaseImpl implements UpdateUomUseCase {

    private final UomRepository repository;

    public UpdateUomUseCaseImpl(UomRepository repository) {
        this.repository = repository;
    }

    @Override
    public UnitOfMeasure execute(Long id, String name, UomType type) {
        UnitOfMeasure uom = repository.findById(id)
            .orElseThrow(() -> new DomainException("msg.error.uom.notfound"));
        uom.update(name, type);
        return repository.save(uom);
    }
}
