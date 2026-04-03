package com.solusi.erp.master.party.application.usecase.query;

import com.solusi.erp.master.party.domain.model.Party;
import com.solusi.erp.master.party.domain.repository.PartyRepository;

import java.util.List;

public class FindPartiesForLookupUseCaseImpl implements FindPartiesForLookupUseCase {

    private final PartyRepository repository;

    public FindPartiesForLookupUseCaseImpl(PartyRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Party> execute(String keyword) {
        return repository.findForLookup(keyword);
    }
}
