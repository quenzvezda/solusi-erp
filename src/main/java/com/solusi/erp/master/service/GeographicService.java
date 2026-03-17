package com.solusi.erp.master.service;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.dto.GeographicRequest;
import com.solusi.erp.master.dto.GeographicResponse;
import com.solusi.erp.master.model.GeographicType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GeographicService {
    Page<GeographicResponse> getAllGeographics(String keyword, Pageable pageable);

    Page<GeographicResponse> getByParent(Long parentId, Pageable pageable);

    GeographicResponse getById(Long id);

    GeographicRequest getEditData(Long id);

    void create(GeographicRequest request);

    void update(Long id, GeographicRequest request);

    void delete(Long id);

    // Lookup methods for TomSelect autocomplete
    List<LookupDto> lookupCountries(String q, int limit);

    List<LookupDto> lookupProvinces(Long countryId, String q, int limit);

    List<LookupDto> lookupCities(Long provinceId, String q, int limit);

    LookupDto getLookupById(Long id);

    List<GeographicResponse> findByType(GeographicType type);
}
