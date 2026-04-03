package com.solusi.erp.master.partyroletype.infrastructure.adapter;

import com.solusi.erp.master.party.infrastructure.persistence.PartyJpaRepository;
import com.solusi.erp.master.partyroletype.domain.port.PartyRoleTypeInUseChecker;

/**
 * Checks if a PartyRoleType is in use by any Party (via party_roles join table).
 */
public class PartyRoleTypeInUseCheckerImpl implements PartyRoleTypeInUseChecker {

    private final PartyJpaRepository partyJpaRepository;

    public PartyRoleTypeInUseCheckerImpl(PartyJpaRepository partyJpaRepository) {
        this.partyJpaRepository = partyJpaRepository;
    }

    @Override
    public boolean isInUse(Long partyRoleTypeId) {
        return partyJpaRepository.existsByRolesId(partyRoleTypeId);
    }
}
