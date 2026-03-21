package com.solusi.erp.master.service;

import com.solusi.erp.core.dto.FormViewDto;
import com.solusi.erp.master.dto.PartyRoleTypeRequest;
import com.solusi.erp.master.dto.PartyRoleTypeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for PartyRoleType CRUD management.
 */
public interface PartyRoleTypeService {
    Page<PartyRoleTypeResponse> findAll(String keyword, Pageable pageable);

    PartyRoleTypeResponse findById(Long id);

    PartyRoleTypeRequest getEditData(Long id);

    FormViewDto<PartyRoleTypeRequest, Void, PartyRoleTypeResponse> getEditView(Long id);

    PartyRoleTypeResponse create(PartyRoleTypeRequest request);

    PartyRoleTypeResponse update(Long id, PartyRoleTypeRequest request);

    void delete(Long id);
}
