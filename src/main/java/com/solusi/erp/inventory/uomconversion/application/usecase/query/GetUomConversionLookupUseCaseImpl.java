package com.solusi.erp.inventory.uomconversion.application.usecase.query;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.uom.infrastructure.persistence.UomEntity;
import com.solusi.erp.inventory.uom.infrastructure.persistence.UomJpaRepository;
import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.product.infrastructure.persistence.ProductEntity;
import com.solusi.erp.inventory.uomconversion.domain.model.UomConversion;
import com.solusi.erp.inventory.uomconversion.domain.repository.UomConversionRepository;
import com.solusi.erp.inventory.uomconversion.web.dto.UomConversionLookupData;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class GetUomConversionLookupUseCaseImpl implements GetUomConversionLookupUseCase {

    private final UomConversionRepository repository;
    private final JpaProductRepository productRepo;
    private final UomJpaRepository uomRepo;

    public GetUomConversionLookupUseCaseImpl(UomConversionRepository repository,
                                              JpaProductRepository productRepo,
                                              UomJpaRepository uomRepo) {
        this.repository = repository;
        this.productRepo = productRepo;
        this.uomRepo = uomRepo;
    }

    @Override
    public List<UomConversionLookupData> getConversionsForProduct(Long productId) {
        ProductEntity product = productRepo.findById(productId)
            .orElseThrow(() -> new DomainException("msg.error.product.not-found"));

        Long baseUomId = product.getUomId();
        UomEntity baseUom = uomRepo.findById(baseUomId)
            .orElseThrow(() -> new DomainException("msg.error.uom.not-found"));
        List<UomConversionLookupData> result = new ArrayList<>();
        result.add(new UomConversionLookupData(
            baseUom.getId(), baseUom.getName(), baseUom.getCode(), BigDecimal.ONE, true));

        List<UomConversion> conversions = repository.findByProductId(productId);
        for (UomConversion conv : conversions) {
            UomEntity fromUom = uomRepo.findById(conv.getFromUomId()).orElse(null);
            String code = fromUom != null ? fromUom.getCode() : "";
            result.add(new UomConversionLookupData(
                conv.getFromUomId(), conv.getFromUomName(), code, conv.getConversionFactor(), false));
        }
        return result;
    }
}
