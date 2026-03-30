package com.solusi.erp.master.party.application.usecase.query;

import com.solusi.erp.master.party.domain.model.Party;
import com.solusi.erp.master.party.domain.repository.PartyRepository;

import java.util.List;

public class FindPartiesAvailableForUserUseCaseImpl implements FindPartiesAvailableForUserUseCase {

    private final PartyRepository repository;

    public FindPartiesAvailableForUserUseCaseImpl(PartyRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Party> execute(String keyword, Long excludePartyId) {
        return repository.findAvailableForUser(keyword, excludePartyId);
    }
}
