package com.solusi.erp.inventory.uomconversion.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.uomconversion.domain.model.UomConversion;
import com.solusi.erp.inventory.uomconversion.domain.repository.UomConversionRepository;

public class FindUomConversionsUseCaseImpl implements FindUomConversionsUseCase {

    private final UomConversionRepository repository;

    public FindUomConversionsUseCaseImpl(UomConversionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<UomConversion> execute(String keyword, Pageable pageable) {
        return repository.findAll(keyword, pageable);
    }
}
