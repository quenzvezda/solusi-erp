package com.solusi.erp.master.tax.web.controller;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.tax.application.usecase.query.GetTaxLookupUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lookup/master/taxes")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('LOOKUP_TAX')")
public class TaxLookupController {

    private final GetTaxLookupUseCase getTaxLookupUseCase;

    @GetMapping
    public List<LookupDto> search(@RequestParam(value = "q", defaultValue = "") String q,
                                  @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return getTaxLookupUseCase.search(q, limit);
    }

    @GetMapping("/{id}")
    public LookupDto getById(@PathVariable Long id) {
        return getTaxLookupUseCase.getById(id);
    }
}
