package com.solusi.erp.master.partyroletype.application.usecase.query;

import com.solusi.erp.master.partyroletype.domain.model.PartyRoleType;

import java.util.Optional;

@FunctionalInterface
public interface GetPartyRoleTypeEditViewUseCase {
    Optional<PartyRoleType> execute(Long id);
}
