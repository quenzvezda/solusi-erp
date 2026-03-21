package com.solusi.erp.inventory.service;

import com.solusi.erp.core.dto.FormViewDto;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.dto.FacilityRequest;
import com.solusi.erp.inventory.dto.FacilityResponse;
import com.solusi.erp.inventory.form.FacilityUIForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service for Facility.
 */
public interface FacilityService {
    LookupDto getLookupFacility(Long id);
    List<FacilityResponse> findAll();
    List<LookupDto> lookupFacilities(String keyword, int limit);
    Page<FacilityResponse> findAll(String keyword, Pageable pageable);
    FacilityResponse findById(Long id);
    FacilityRequest getEditData(Long id);
    FormViewDto<FacilityRequest, FacilityUIForm, FacilityResponse> getFormView(Long id);
    FacilityResponse create(FacilityRequest request);
    FacilityResponse update(Long id, FacilityRequest request);
    void delete(Long id);
}
