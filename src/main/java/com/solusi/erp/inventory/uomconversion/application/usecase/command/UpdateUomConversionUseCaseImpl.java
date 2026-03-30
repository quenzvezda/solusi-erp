package com.solusi.erp.inventory.uomconversion.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.uom.infrastructure.persistence.UomEntity;
import com.solusi.erp.inventory.uom.infrastructure.persistence.UomJpaRepository;
import com.solusi.erp.inventory.uomconversion.domain.model.UomConversion;
import com.solusi.erp.inventory.uomconversion.domain.repository.UomConversionRepository;

import java.math.BigDecimal;

public class UpdateUomConversionUseCaseImpl implements UpdateUomConversionUseCase {

    private final UomConversionRepository repository;
    private final UomJpaRepository uomRepo;

    public UpdateUomConversionUseCaseImpl(UomConversionRepository repository,
                                          UomJpaRepository uomRepo) {
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

        UomEntity fromUom = uomRepo.findById(fromUomId)
            .orElseThrow(() -> new DomainException("msg.error.uom.not-found"));

        existing.update(fromUomId, fromUom.getName(), conversionFactor);
        return repository.save(existing);
    }
}
