package com.solusi.erp.inventory.grid.web.controller;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.grid.application.usecase.query.GetGridLookupUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lookup/inventory/grids")
@RequiredArgsConstructor
public class GridLookupController {

    private final GetGridLookupUseCase getGridLookupUseCase;

    @GetMapping
    @PreAuthorize("hasAuthority('LOOKUP_GRID')")
    public List<LookupDto> search(@RequestParam(value = "q", defaultValue = "") String q,
                                  @RequestParam(required = false) Long facilityId,
                                  @RequestParam(defaultValue = "10") int limit) {
        return getGridLookupUseCase.search(q, facilityId, limit);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LOOKUP_GRID')")
    public LookupDto getById(@PathVariable Long id) {
        return getGridLookupUseCase.getById(id);
    }
}
