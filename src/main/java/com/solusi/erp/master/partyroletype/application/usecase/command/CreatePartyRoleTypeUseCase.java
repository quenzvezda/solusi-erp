package com.solusi.erp.master.partyroletype.application.usecase.command;

import com.solusi.erp.master.partyroletype.domain.model.PartyRoleType;

@FunctionalInterface
public interface CreatePartyRoleTypeUseCase {
    PartyRoleType execute(String code, String name, String note, Boolean isActive);
}
