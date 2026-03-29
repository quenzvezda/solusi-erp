package com.solusi.erp.inventory.facility.web.controller;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.facility.application.usecase.query.GetFacilityLookupUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lookup/inventory/facilities")
@RequiredArgsConstructor
public class FacilityLookupController {

    private final GetFacilityLookupUseCase getFacilityLookupUseCase;

    @GetMapping
    @PreAuthorize("hasAuthority('LOOKUP_FACILITY')")
    public List<LookupDto> search(@RequestParam(value = "q", defaultValue = "") String q,
                                  @RequestParam(defaultValue = "10") int limit) {
        return getFacilityLookupUseCase.search(q, limit);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LOOKUP_FACILITY')")
    public LookupDto getById(@PathVariable Long id) {
        return getFacilityLookupUseCase.getById(id);
    }
}
