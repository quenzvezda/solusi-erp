package com.solusi.erp.master.party.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.master.party.domain.model.Party;

import java.util.List;
import java.util.Optional;

public interface PartyRepository {
    Party save(Party party);
    Optional<Party> findById(Long id);
    Page<Party> findAll(String keyword, Pageable pageable);
    List<Party> findForLookup(String keyword);
    List<Party> findAvailableForUser(String keyword, Long excludePartyId);
    void delete(Long id);
    void softDelete(Long id);
}
