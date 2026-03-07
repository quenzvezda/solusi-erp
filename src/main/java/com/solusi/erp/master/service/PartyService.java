package com.solusi.erp.master.service;

import com.solusi.erp.master.dto.PartyRequest;
import com.solusi.erp.master.dto.PartyResponse;
import com.solusi.erp.master.model.PartyIdentificationType;
import com.solusi.erp.master.model.PartyRoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for Party (Business Partner).
 */
public interface PartyService {
    Page<PartyResponse> findAll(String keyword, Pageable pageable);

    PartyResponse findById(Long id);

    PartyRequest getEditData(Long id);

    void create(PartyRequest request);

    void update(Long id, PartyRequest request);

    void delete(Long id);

    // Lookup data
    List<PartyRoleType> findAllRoleTypes();

    List<PartyIdentificationType> findAllIdTypes();
}
