package com.solusi.erp.master.party.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for PartyContact (ContactMechanism).
 */
@Repository
public interface PartyContactJpaRepository extends JpaRepository<PartyContact, Long> {

    List<PartyContact> findByPartyIdAndIsActiveTrue(Long partyId);

    boolean existsByPartyIdAndIsDefaultTrueAndIdNot(Long partyId, Long excludeId);
}

