package com.solusi.erp.inventory.service;

import com.solusi.erp.inventory.dto.FacilityRequest;
import com.solusi.erp.inventory.dto.FacilityResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service for Facility.
 */
public interface FacilityService {
    Page<FacilityResponse> findAll(String keyword, Pageable pageable);
    FacilityResponse findById(Long id);
    FacilityRequest getEditData(Long id);
    void create(FacilityRequest request);
    void update(Long id, FacilityRequest request);
    void delete(Long id);
}
