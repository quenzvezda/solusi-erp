package com.solusi.erp.security.user.infrastructure.adapter;

import com.solusi.erp.master.party.application.dto.PartyReference;
import com.solusi.erp.master.party.application.usecase.query.GetPartyReferenceUseCase;
import com.solusi.erp.security.user.application.port.PartyReferenceGateway;

import java.util.Optional;

public class PartyReferenceGatewayAdapter implements PartyReferenceGateway {

    private final GetPartyReferenceUseCase getPartyReferenceUseCase;

    public PartyReferenceGatewayAdapter(GetPartyReferenceUseCase getPartyReferenceUseCase) {
        this.getPartyReferenceUseCase = getPartyReferenceUseCase;
    }

    @Override
    public Optional<PartyReferenceData> findById(Long partyId) {
        return getPartyReferenceUseCase.execute(partyId)
                .map(this::toData);
    }

    private PartyReferenceData toData(PartyReference ref) {
        return new PartyReferenceData(ref.id(), ref.code(), ref.name());
    }
}
