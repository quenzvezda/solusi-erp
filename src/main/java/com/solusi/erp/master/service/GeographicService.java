package com.solusi.erp.master.service;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.dto.GeographicDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GeographicService {
    Page<GeographicDto> getAllGeographics(String keyword, Pageable pageable);

    Page<GeographicDto> getByParent(Long parentId, Pageable pageable);

    GeographicDto getById(Long id);

    GeographicDto create(GeographicDto dto);

    GeographicDto update(Long id, GeographicDto dto);

    void delete(Long id);

    // Lookup methods for TomSelect autocomplete
    List<LookupDto> lookupCountries(String q, int limit);

    List<LookupDto> lookupProvinces(Long countryId, String q, int limit);

    List<LookupDto> lookupCities(Long provinceId, String q, int limit);
}
