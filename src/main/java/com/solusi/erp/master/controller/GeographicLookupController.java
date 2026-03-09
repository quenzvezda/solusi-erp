package com.solusi.erp.master.controller;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.service.GeographicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/lookup/geographics")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('LOOKUP_GEOGRAPHIC')")
public class GeographicLookupController {

    private final GeographicService geographicService;

    @GetMapping("/countries")
    public List<LookupDto> lookupCountries(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "5") int limit) {
        return geographicService.lookupCountries(q, limit);
    }

    @GetMapping("/provinces")
    public List<LookupDto> lookupProvinces(
            @RequestParam(required = false) Long countryId,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "5") int limit) {
        return geographicService.lookupProvinces(countryId, q, limit);
    }

    @GetMapping("/cities")
    public List<LookupDto> lookupCities(
            @RequestParam(required = false) Long provinceId,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "5") int limit) {
        return geographicService.lookupCities(provinceId, q, limit);
    }
}
