package com.solusi.erp.master.party.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.master.party.domain.model.Party;
import com.solusi.erp.master.party.domain.repository.PartyRepository;

public class FindPartiesUseCaseImpl implements FindPartiesUseCase {

    private final PartyRepository repository;

    public FindPartiesUseCaseImpl(PartyRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Party> execute(String keyword, Pageable pageable) {
        return repository.findAll(keyword, pageable);
    }
}
