package com.solusi.erp.inventory.product.web.controller;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.product.application.usecase.query.GetProductLookupUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Inventory Lookups.
 * Refactored to use Clean DDD Use Cases for Product.
 */
@RestController
@RequestMapping("/inventory/lookup")
@RequiredArgsConstructor
public class InventoryLookupController {

    private final GetProductLookupUseCase getProductLookupUseCase;

    @GetMapping("/products")
    public List<LookupDto> lookupProducts(@RequestParam(required = false) String keyword, 
                                        @RequestParam(defaultValue = "10") int limit) {
        return getProductLookupUseCase.search(keyword, limit);
    }

    @GetMapping("/products/{id}")
    public LookupDto getLookupProduct(@PathVariable Long id) {
        return getProductLookupUseCase.getById(id);
    }
}
