package com.solusi.erp.inventory.uomconversion.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.uomconversion.domain.repository.UomConversionRepository;

public class DeleteUomConversionUseCaseImpl implements DeleteUomConversionUseCase {

    private final UomConversionRepository repository;

    public DeleteUomConversionUseCaseImpl(UomConversionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(Long id) {
        if (!repository.existsById(id)) {
            throw new DomainException("msg.error.uom-conversion.not-found");
        }
        repository.deleteById(id);
    }
}
