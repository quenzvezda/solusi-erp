package com.solusi.erp.master.partyroletype.domain.port;

public interface PartyRoleTypeInUseChecker {
    boolean isInUse(Long partyRoleTypeId);
}
