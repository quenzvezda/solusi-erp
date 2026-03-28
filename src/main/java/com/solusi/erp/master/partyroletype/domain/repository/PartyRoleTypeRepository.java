package com.solusi.erp.master.partyroletype.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.master.partyroletype.domain.model.PartyRoleType;

import java.util.Optional;

/**
 * Domain Repository interface for PartyRoleType.
 * Pure Java — no framework dependency.
 */
public interface PartyRoleTypeRepository {
    PartyRoleType save(PartyRoleType partyRoleType);
    Optional<PartyRoleType> findById(Long id);
    Page<PartyRoleType> findAll(String keyword, Pageable pageable);
    void delete(Long id);
}
