package com.solusi.erp.inventory.uomconversion.application.usecase.query;

import com.solusi.erp.inventory.uomconversion.domain.model.UomConversion;
import com.solusi.erp.inventory.uomconversion.domain.repository.UomConversionRepository;

import java.util.Optional;

public class GetUomConversionEditViewUseCaseImpl implements GetUomConversionEditViewUseCase {

    private final UomConversionRepository repository;

    public GetUomConversionEditViewUseCaseImpl(UomConversionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<UomConversion> execute(Long id) {
        return repository.findById(id);
    }
}
