package com.solusi.erp.inventory.uomconversion.application.usecase.command;

import com.solusi.erp.inventory.uomconversion.domain.model.UomConversion;

import java.math.BigDecimal;

public interface UpdateUomConversionUseCase {
    UomConversion execute(Long id, Long fromUomId, BigDecimal conversionFactor);
}
