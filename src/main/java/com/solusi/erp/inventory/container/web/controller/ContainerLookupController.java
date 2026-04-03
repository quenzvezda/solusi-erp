package com.solusi.erp.inventory.container.web.controller;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.container.application.usecase.query.GetContainerLookupUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lookup/inventory/containers")
@RequiredArgsConstructor
public class ContainerLookupController {

    private final GetContainerLookupUseCase getContainerLookupUseCase;

    @GetMapping
    @PreAuthorize("hasAuthority('LOOKUP_CONTAINER')")
    public List<LookupDto> search(@RequestParam(value = "q", defaultValue = "") String q,
                                  @RequestParam(required = false) Long gridId,
                                  @RequestParam(required = false) Long facilityId,
                                  @RequestParam(defaultValue = "10") int limit) {
        return getContainerLookupUseCase.search(q, gridId, facilityId, limit);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LOOKUP_CONTAINER')")
    public LookupDto getById(@PathVariable Long id) {
        return getContainerLookupUseCase.getById(id);
    }
}
