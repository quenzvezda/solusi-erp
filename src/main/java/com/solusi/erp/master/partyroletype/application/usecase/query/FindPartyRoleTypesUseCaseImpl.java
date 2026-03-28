package com.solusi.erp.master.partyroletype.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.master.partyroletype.domain.model.PartyRoleType;
import com.solusi.erp.master.partyroletype.domain.repository.PartyRoleTypeRepository;

public class FindPartyRoleTypesUseCaseImpl implements FindPartyRoleTypesUseCase {

    private final PartyRoleTypeRepository repository;

    public FindPartyRoleTypesUseCaseImpl(PartyRoleTypeRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<PartyRoleType> execute(String keyword, Pageable pageable) {
        return repository.findAll(keyword, pageable);
    }
}
