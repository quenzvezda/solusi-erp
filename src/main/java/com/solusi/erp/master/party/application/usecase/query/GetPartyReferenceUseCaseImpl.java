package com.solusi.erp.master.party.application.usecase.query;

import com.solusi.erp.master.party.application.dto.PartyReference;
import com.solusi.erp.master.party.domain.repository.PartyRepository;

import java.util.Optional;

public class GetPartyReferenceUseCaseImpl implements GetPartyReferenceUseCase {

    private final PartyRepository repository;

    public GetPartyReferenceUseCaseImpl(PartyRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PartyReference> execute(Long id) {
        return repository.findById(id)
                .map(p -> new PartyReference(p.getMetadata().id(), p.getCode(), p.getName()));
    }
}
