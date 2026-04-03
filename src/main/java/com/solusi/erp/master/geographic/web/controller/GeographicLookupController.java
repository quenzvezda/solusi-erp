package com.solusi.erp.master.geographic.web.controller;

import com.solusi.erp.master.geographic.application.usecase.query.*;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.geographic.domain.model.Geographic;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for Geographic dropdown lookups (AJAX).
 */
@RestController
@RequestMapping("/api/lookup/geographics")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('LOOKUP_GEOGRAPHIC')")
public class GeographicLookupController {

    private final FindCountriesUseCase findCountriesUseCase;
    private final FindProvincesByCountryUseCase findProvincesByCountryUseCase;
    private final FindCitiesByProvinceUseCase findCitiesByProvinceUseCase;
    @GetMapping("/countries")
    public List<LookupDto> getCountries(
            @RequestParam(value = "q", defaultValue = "") String q,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return findCountriesUseCase.execute(q, limit).stream()
                .map(this::toLookupDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/provinces")
    public List<LookupDto> getProvinces(
            @RequestParam(required = false) Long countryId,
            @RequestParam(value = "q", defaultValue = "") String q,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return findProvincesByCountryUseCase.execute(countryId, q, limit).stream()
                .map(this::toLookupDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/cities")
    public List<LookupDto> getCities(
            @RequestParam(required = false) Long provinceId,
            @RequestParam(value = "q", defaultValue = "") String q,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return findCitiesByProvinceUseCase.execute(provinceId, q, limit).stream()
                .map(this::toLookupDto)
                .collect(Collectors.toList());
    }

    private LookupDto toLookupDto(Geographic geographic) {
        return new LookupDto(
                geographic.getId(),
                geographic.getName(),
                geographic.getCode(),
                Map.of(
                        "code", geographic.getCode(),
                        "type", geographic.getType() != null ? geographic.getType().name() : "",
                        "parentId", geographic.getParentId() != null ? geographic.getParentId() : "",
                        "parentName", geographic.getParentName() != null ? geographic.getParentName() : ""
                )
        );
    }
}
