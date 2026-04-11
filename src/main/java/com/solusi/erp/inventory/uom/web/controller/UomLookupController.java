package com.solusi.erp.inventory.uom.web.controller;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.uom.application.usecase.query.GetUomLookupUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lookup/inventory/uoms")
@RequiredArgsConstructor
public class UomLookupController {

    private final GetUomLookupUseCase getUomLookupUseCase;

    @GetMapping
    public List<LookupDto> search(@RequestParam(value = "q", defaultValue = "") String q,
                                  @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return getUomLookupUseCase.search(q, limit);
    }

    @GetMapping("/{id}")
    public LookupDto getById(@PathVariable Long id) {
        return getUomLookupUseCase.getById(id);
    }
}
