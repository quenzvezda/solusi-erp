package com.solusi.erp.inventory.service;

import com.solusi.erp.core.dto.FormViewDto;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.dto.ContainerRequest;
import com.solusi.erp.inventory.dto.ContainerResponse;
import com.solusi.erp.inventory.form.ContainerUIForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service for Container.
 */
public interface ContainerService {
    LookupDto getLookupContainer(Long id);
    List<LookupDto> lookupContainers(String keyword, Long facilityId, Long gridId, int limit);
    List<ContainerResponse> findAll();
    Page<ContainerResponse> findAll(String keyword, Long gridId, Pageable pageable);
    ContainerResponse findById(Long id);
    ContainerRequest getEditData(Long id);
    
    FormViewDto<ContainerRequest, ContainerUIForm, ContainerResponse> getFormView(Long id);
    
    ContainerResponse create(ContainerRequest request);
    
    ContainerResponse update(Long id, ContainerRequest request);
    
    void delete(Long id);
}
