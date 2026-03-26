package com.solusi.erp.inventory.service.impl;

import com.solusi.erp.inventory.product.infrastructure.persistence.ProductEntity;
import com.solusi.erp.inventory.model.ProductUomConversion;
import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.repository.ProductUomConversionRepository;
import com.solusi.erp.inventory.service.UomConversionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UomConversionServiceImpl implements UomConversionService {

    private final JpaProductRepository productRepository;
    private final ProductUomConversionRepository conversionRepository;
    private final MessageSource messageSource;

    @Override
    public BigDecimal convertToBaseUom(Long productId, Long sourceUomId, BigDecimal quantity) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.product.notfound")));

        Long baseUomId = product.getUom().getId();

        // If source is already base, no conversion needed
        if (sourceUomId.equals(baseUomId)) {
            return quantity;
        }

        // Lookup conversion factor
        ProductUomConversion conversion = conversionRepository
                .findByProductIdAndFromUomIdAndToUomId(productId, sourceUomId, baseUomId)
                .orElseThrow(() -> new RuntimeException(
                        getMessage("msg.error.uom.conversion_not_found", 
                                product.getCode(), sourceUomId, baseUomId)));

        return quantity.multiply(conversion.getConversionFactor());
    }

    private String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
