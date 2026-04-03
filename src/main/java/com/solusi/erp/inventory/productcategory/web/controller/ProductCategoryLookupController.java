package com.solusi.erp.inventory.productcategory.web.controller;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.productcategory.application.usecase.query.GetProductCategoryLookupUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lookup/inventory/product-categories")
@RequiredArgsConstructor
public class ProductCategoryLookupController {

    private final GetProductCategoryLookupUseCase getProductCategoryLookupUseCase;

    @GetMapping
    @PreAuthorize("hasAuthority('LOOKUP_PRODUCT-CATEGORY')")
    public List<LookupDto> search(@RequestParam(value = "q", defaultValue = "") String q,
                                  @RequestParam(defaultValue = "10") int limit) {
        return getProductCategoryLookupUseCase.search(q, limit);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LOOKUP_PRODUCT-CATEGORY')")
    public LookupDto getById(@PathVariable Long id) {
        return getProductCategoryLookupUseCase.getById(id);
    }
}
