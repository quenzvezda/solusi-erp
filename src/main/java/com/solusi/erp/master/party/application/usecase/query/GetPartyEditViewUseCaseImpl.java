package com.solusi.erp.master.party.application.usecase.query;

import com.solusi.erp.master.party.domain.model.Party;
import com.solusi.erp.master.party.domain.repository.PartyRepository;

import java.util.Optional;

public class GetPartyEditViewUseCaseImpl implements GetPartyEditViewUseCase {

    private final PartyRepository repository;

    public GetPartyEditViewUseCaseImpl(PartyRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Party> execute(Long id) {
        return repository.findById(id);
    }
}
