package com.solusi.erp.inventory.brand.web.controller;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.brand.application.usecase.query.GetBrandLookupUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lookup/inventory/brands")
@RequiredArgsConstructor
public class BrandLookupController {

    private final GetBrandLookupUseCase getBrandLookupUseCase;

    @GetMapping
    @PreAuthorize("hasAuthority('LOOKUP_BRAND')")
    public List<LookupDto> search(@RequestParam(value = "q", defaultValue = "") String q,
                                  @RequestParam(defaultValue = "10") int limit) {
        return getBrandLookupUseCase.search(q, limit);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LOOKUP_BRAND')")
    public LookupDto getById(@PathVariable Long id) {
        return getBrandLookupUseCase.getById(id);
    }
}
