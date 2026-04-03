package com.solusi.erp.inventory.uomconversion.web.controller;

import com.solusi.erp.inventory.uomconversion.application.usecase.query.GetUomConversionLookupUseCase;
import com.solusi.erp.inventory.uomconversion.web.dto.UomConversionLookupData;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for inventory lookup APIs.
 * Provides the UOM conversion lookup endpoint consumed by shared/erp-common-handler.js.
 */
@RestController
@RequestMapping("/api/lookup/inventory")
@RequiredArgsConstructor
public class InventoryApiLookupController {

    private final GetUomConversionLookupUseCase getUomConversionLookupUseCase;

    @GetMapping("/uom-conversions")
    @PreAuthorize("hasAuthority('LOOKUP_UOM-CONVERSION')")
    public List<UomConversionLookupData> getConversions(@RequestParam Long productId) {
        return getUomConversionLookupUseCase.getConversionsForProduct(productId);
    }
}
