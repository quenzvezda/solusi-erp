package com.solusi.erp.master.service;

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

    void create(PartyRoleTypeRequest request);

    void update(Long id, PartyRoleTypeRequest request);

    void delete(Long id);
}
