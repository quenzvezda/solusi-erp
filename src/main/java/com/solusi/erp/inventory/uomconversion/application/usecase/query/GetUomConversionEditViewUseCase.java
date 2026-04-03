package com.solusi.erp.inventory.uomconversion.application.usecase.query;

import com.solusi.erp.inventory.uomconversion.domain.model.UomConversion;

import java.util.Optional;

public interface GetUomConversionEditViewUseCase {
    Optional<UomConversion> execute(Long id);
}
