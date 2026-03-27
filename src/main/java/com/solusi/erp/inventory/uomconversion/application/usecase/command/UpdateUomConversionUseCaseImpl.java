package com.solusi.erp.inventory.uomconversion.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.model.UnitOfMeasure;
import com.solusi.erp.inventory.repository.UnitOfMeasureRepository;
import com.solusi.erp.inventory.uomconversion.domain.model.UomConversion;
import com.solusi.erp.inventory.uomconversion.domain.repository.UomConversionRepository;

import java.math.BigDecimal;

public class UpdateUomConversionUseCaseImpl implements UpdateUomConversionUseCase {

    private final UomConversionRepository repository;
    private final UnitOfMeasureRepository uomRepo;

    public UpdateUomConversionUseCaseImpl(UomConversionRepository repository,
                                          UnitOfMeasureRepository uomRepo) {
        this.repository = repository;
        this.uomRepo = uomRepo;
    }

    @Override
    public UomConversion execute(Long id, Long fromUomId, BigDecimal conversionFactor) {
        UomConversion existing = repository.findById(id)
            .orElseThrow(() -> new DomainException("msg.error.uom-conversion.not-found"));

        if (repository.existsByProductIdAndFromUomIdAndIdNot(existing.getProductId(), fromUomId, id)) {
            throw new DomainException("msg.error.uom-conversion.duplicate");
        }

        UnitOfMeasure fromUom = uomRepo.findById(fromUomId)
            .orElseThrow(() -> new DomainException("msg.error.uom.not-found"));

        existing.update(fromUomId, fromUom.getName(), conversionFactor);
        return repository.save(existing);
    }
}
