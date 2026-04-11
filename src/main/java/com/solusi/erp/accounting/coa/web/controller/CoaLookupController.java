package com.solusi.erp.accounting.coa.web.controller;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.accounting.coa.application.usecase.query.GetCoaLookupUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lookup/accounting/coa")
@RequiredArgsConstructor
public class CoaLookupController {

    private final GetCoaLookupUseCase getCoaLookupUseCase;

    @GetMapping
    @PreAuthorize("hasAuthority('LOOKUP_COA')")
    public List<LookupDto> search(@RequestParam(value = "q", defaultValue = "") String q,
                                  @RequestParam(defaultValue = "10") int limit) {
        return getCoaLookupUseCase.search(q, limit);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LOOKUP_COA')")
    public LookupDto getById(@PathVariable Long id) {
        return getCoaLookupUseCase.getById(id);
    }
}
