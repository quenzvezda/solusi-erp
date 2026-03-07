package com.solusi.erp.master.repository;

import com.solusi.erp.master.model.PartyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for PartyContact (ContactMechanism).
 */
@Repository
public interface PartyContactRepository extends JpaRepository<PartyContact, Long> {

    List<PartyContact> findByPartyIdAndIsActiveTrue(Long partyId);

    boolean existsByPartyIdAndIsDefaultTrueAndIdNot(Long partyId, Long excludeId);
}
