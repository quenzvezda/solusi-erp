package com.solusi.erp.master.party.application.usecase.query;

import com.solusi.erp.master.party.application.dto.PartyReference;

import java.util.Optional;

@FunctionalInterface
public interface GetPartyReferenceUseCase {
    Optional<PartyReference> execute(Long id);
}
