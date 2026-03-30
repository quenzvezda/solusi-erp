package com.solusi.erp.inventory.uomconversion.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.uom.infrastructure.persistence.UomEntity;
import com.solusi.erp.inventory.uom.infrastructure.persistence.UomJpaRepository;
import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.product.infrastructure.persistence.ProductEntity;
import com.solusi.erp.inventory.uomconversion.domain.model.UomConversion;
import com.solusi.erp.inventory.uomconversion.domain.repository.UomConversionRepository;

import java.math.BigDecimal;

public class CreateUomConversionUseCaseImpl implements CreateUomConversionUseCase {

    private final UomConversionRepository repository;
    private final JpaProductRepository productRepo;
    private final UomJpaRepository uomRepo;

    public CreateUomConversionUseCaseImpl(UomConversionRepository repository,
                                          JpaProductRepository productRepo,
                                          UomJpaRepository uomRepo) {
        this.repository = repository;
        this.productRepo = productRepo;
        this.uomRepo = uomRepo;
    }

    @Override
    public UomConversion execute(Long productId, Long fromUomId, BigDecimal conversionFactor) {
        ProductEntity product = productRepo.findById(productId)
            .orElseThrow(() -> new DomainException("msg.error.product.not-found"));

        Long baseUomId = product.getUomId();
        if (fromUomId.equals(baseUomId)) {
            throw new DomainException("msg.error.uom-conversion.self-conversion");
        }

        if (repository.existsByProductIdAndFromUomId(productId, fromUomId)) {
            throw new DomainException("msg.error.uom-conversion.duplicate");
        }

        UomEntity baseUom = uomRepo.findById(baseUomId)
            .orElseThrow(() -> new DomainException("msg.error.uom.not-found"));

        UomEntity fromUom = uomRepo.findById(fromUomId)
            .orElseThrow(() -> new DomainException("msg.error.uom.not-found"));

        UomConversion domain = UomConversion.createNew(
            productId, product.getCode(), product.getName(),
            fromUomId, fromUom.getName(),
            baseUomId, baseUom.getName(),
            conversionFactor
        );

        return repository.save(domain);
    }
}
