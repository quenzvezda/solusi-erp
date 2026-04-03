package com.solusi.erp.inventory.product.web.controller;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.product.application.usecase.query.GetProductLookupUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lookup/inventory/products")
@RequiredArgsConstructor
public class ProductLookupController {

    private final GetProductLookupUseCase getProductLookupUseCase;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<LookupDto> search(@RequestParam(value = "q", defaultValue = "") String q,
                                  @RequestParam(defaultValue = "10") int limit) {
        return getProductLookupUseCase.search(q, limit);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public LookupDto getById(@PathVariable Long id) {
        return getProductLookupUseCase.getById(id);
    }
}
