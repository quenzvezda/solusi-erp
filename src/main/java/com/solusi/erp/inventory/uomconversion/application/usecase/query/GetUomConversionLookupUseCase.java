package com.solusi.erp.inventory.uomconversion.application.usecase.query;

import com.solusi.erp.inventory.uomconversion.web.dto.UomConversionLookupData;

import java.util.List;

public interface GetUomConversionLookupUseCase {
    List<UomConversionLookupData> getConversionsForProduct(Long productId);
}
