package com.solusi.erp.master.partyroletype.application.usecase.command;

import com.solusi.erp.master.partyroletype.domain.model.PartyRoleType;

@FunctionalInterface
public interface UpdatePartyRoleTypeUseCase {
    PartyRoleType execute(Long id, String name, String note, Boolean isActive);
}
