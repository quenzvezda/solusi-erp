package com.solusi.erp.master.party.application.usecase.query;

import com.solusi.erp.master.party.domain.model.Party;

import java.util.Optional;

@FunctionalInterface
public interface GetPartyEditViewUseCase {
    Optional<Party> execute(Long id);
}
