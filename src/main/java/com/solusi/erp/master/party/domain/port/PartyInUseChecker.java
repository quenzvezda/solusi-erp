package com.solusi.erp.master.party.domain.port;

public interface PartyInUseChecker {
    boolean isInUse(Long partyId);
}
